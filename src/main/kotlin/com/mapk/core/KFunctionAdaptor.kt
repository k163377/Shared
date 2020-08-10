package com.mapk.core

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

class KFunctionAdaptor<T>(
    private val function: KFunction<T>,
    private val index: Int?,
    private val valueParameters: List<KParameter>, // name, index, annotations, ...
    private val bucket: Array<Any?>,
    private val children: List<KFunctionAdaptor<*>>
) {
    val requiredParameterMap: Map<String, Pair<KParameter, Array<Any?>>>

    init {
        val thisMap = valueParameters.associate {
            it.name!! to (it to bucket)
        }

        requiredParameterMap = thisMap + children.map { it.requiredParameterMap }.reduce { acc, cur -> acc + cur }
    }

    fun isFullInitialized(): Boolean = TODO("ArgumentBucketちゃんと作ったら")

    fun isInitialized(key: String): Boolean = TODO("もしかするとput対象返した方が効率的？")

    fun putIfAbsent(key: String, value: Any?) {
        if (isInitialized(key)) return

        requiredParameterMap.getValue(key).let { (param, bucket) ->
            bucket[param.index] = value
        }
    }

    fun putIfAbsent(key: String, consumer: () -> Any?) {
        if (isInitialized(key)) return

        requiredParameterMap.getValue(key).let { (param, bucket) ->
            bucket[param.index] = consumer()
        }
    }

    fun forcePut(key: String, value: Any?) {
        requiredParameterMap.getValue(key).let { (param, bucket) ->
            bucket[param.index] = value
        }
    }

    fun call(): T {
        children.forEach {
            // 子ならindexは入ってる想定
            bucket[it.index!!] = it.call()
        }
        // TODO: Bucket呼び出し
        return function.call(*bucket)
    }
}
