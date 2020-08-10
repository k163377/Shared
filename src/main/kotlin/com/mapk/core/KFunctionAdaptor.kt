package com.mapk.core

import com.mapk.core.internal.ArgumentBucket
import com.mapk.core.internal.ValueParameterGenerator
import kotlin.reflect.KFunction

class KFunctionAdaptor<T> internal constructor(
    private val function: KFunction<T>,
    private val index: Int?,
    valueParameters: List<ValueParameterGenerator<*>>,
    private val bucket: ArgumentBucket,
    private val children: List<KFunctionAdaptor<*>>
) {
    val requiredParameters: List<ValueParameter<*>> = valueParameters.map { it.generate(bucket) }
    private val requiredParameterMap: Map<String, ValueParameter<*>>

    init {
        val thisMap = requiredParameters.associateBy { it.name }

        requiredParameterMap = children
            .map { it.requiredParameterMap }
            .fold(thisMap) { acc, cur -> acc + cur }
    }

    val isFullInitialized: Boolean get() = bucket.isFullInitialized

    // TODO: forcePutとか考えると、初期化状況の管理はAdaptorに持たせるのが妥当という気がするので考える
    fun putIfAbsent(key: String, value: Any?) {
        requiredParameterMap.getValue(key).let {
            // TODO: bucketのインターフェースを実情に合わせて検討する
            if (!it.bucket.containsKey(it.parameter)) it.bucket.putIfAbsent(it.parameter.index, value)
        }
    }

    fun putIfAbsent(key: String, consumer: () -> Any?) {
        requiredParameterMap.getValue(key).let {
            if (!it.bucket.containsKey(it.parameter)) it.bucket.putIfAbsent(it.parameter.index, consumer())
        }
    }

    fun call(): T {
        children.forEach {
            // 子ならindexは入ってる想定
            bucket.putIfAbsent(it.index!!, it.call())
        }

        return if (bucket.isFullInitialized)
            function.call(*bucket.valueArray)
        else
            function.call(bucket)
    }
}
