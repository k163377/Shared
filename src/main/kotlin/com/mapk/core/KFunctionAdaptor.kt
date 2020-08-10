package com.mapk.core

import com.mapk.core.internal.ArgumentBucket
import kotlin.reflect.KFunction

class KFunctionAdaptor<T> internal constructor(
    private val function: KFunction<T>,
    private val index: Int?,
    myParameters: List<ValueParameter<*>>,
    private val myBucket: ArgumentBucket,
    private val children: List<KFunctionAdaptor<*>>
) {
    private var count = myBucket.initialCount
    private val requiredParameterMap: Map<String, Pair<ValueParameter<*>, ArgumentBucket>>

    init {
        val thisMap = myParameters.associate { it.name to (it to myBucket) }

        requiredParameterMap = children
            .map { it.requiredParameterMap }
            .fold(thisMap) { acc, cur -> acc + cur }
    }

    // TODO: ここでいう初期化状態は子まで全て含んだものになるため修正する
    val isFullInitialized: Boolean get() = myBucket.valueArray.size == count

    fun putIfAbsent(key: String, value: Any?) {
        requiredParameterMap.getValue(key).let { (param, bucket) ->
            if (!bucket.initializationStatuses[param.index]) {
                bucket.forcePut(param.index, value)
                count++
            }
        }
    }

    fun putIfAbsent(key: String, consumer: () -> Any?) {
        requiredParameterMap.getValue(key).let { (param, bucket) ->
            if (!bucket.initializationStatuses[param.index]) {
                bucket.forcePut(param.index, consumer())
                count++
            }
        }
    }

    // TODO: forcePut関数を作る

    fun call(): T {
        children.forEach {
            // 子ならindexは入ってる想定
            myBucket.forcePut(it.index!!, it.call())
        }

        return if (isFullInitialized)
            function.call(*myBucket.valueArray)
        else
            function.call(myBucket)
    }
}
