package com.mapk.core.internal

import kotlin.reflect.KParameter

internal class BucketGenerator(private val keyList: List<KParameter>, instance: Any?) {
    private val originalInitializationStatus: Array<Boolean>
    private val originalValueArray: Array<Any?>
    private val originalCount: Int

    init {
        val capacity = keyList.size

        originalInitializationStatus = Array(capacity) { false }
        originalValueArray = arrayOfNulls(capacity)

        if (instance != null) {
            originalValueArray[0] = instance
            originalInitializationStatus[0] = true
            originalCount = 1
        } else {
            originalCount = 0
        }
    }

    fun generate(): ArgumentBucket {
        return ArgumentBucket(
            keyList,
            originalValueArray.clone(),
            originalInitializationStatus.clone(),
            originalCount
        )
    }
}
