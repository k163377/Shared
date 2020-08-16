package com.mapk.core

import com.mapk.core.internal.ArgumentBucket
import kotlin.reflect.KFunction

class KFunctionAdaptor<T> internal constructor(
    private val function: KFunction<T>,
    private val index: Int?, // Adaptor内で子関数として呼び出す際に利用するindex
    private var count: Int, // バケツ内のカウント周りに合わせるため、外部から受け取る
    myParameters: List<ValueParameter<*>>,
    private val myBucket: ArgumentBucket,
    private val children: List<KFunctionAdaptor<*>>
) {
    private val requiredParameterMap: Map<String, Pair<Int, ArgumentBucket>>

    init {
        val thisMap = HashMap<String, Pair<Int, ArgumentBucket>>()
        myParameters.forEach { thisMap[it.name] = it.index to myBucket }

        requiredParameterMap = children
            .map { it.requiredParameterMap }
            .fold(thisMap) { acc, cur -> acc.apply { putAll(cur) } }
    }

    val isFullInitialized: Boolean get() = count == 0 && children.all { it.isFullInitialized }

    fun putIfAbsent(key: String, value: Any?) {
        requiredParameterMap.getValue(key).let { (param, bucket) ->
            if (!bucket.initializationStatuses[param]) {
                bucket.forcePut(param, value)
                count--
            }
        }
    }

    fun putIfAbsent(key: String, consumer: () -> Any?) {
        requiredParameterMap.getValue(key).let { (index, bucket) ->
            if (!bucket.initializationStatuses[index]) {
                bucket.forcePut(index, consumer())
                count--
            }
        }
    }

    fun forcePut(key: String, value: Any?) {
        requiredParameterMap.getValue(key).let { (index, bucket) ->
            bucket.forcePut(index, value)
            count--
        }
    }

    fun call(): T {
        children.forEach {
            // 子ならindexは入ってる想定
            myBucket.forcePut(it.index!!, it.call())
        }

        return if (isFullInitialized)
            function.call(*myBucket.valueArray)
        else
            function.callBy(myBucket)
    }
}
