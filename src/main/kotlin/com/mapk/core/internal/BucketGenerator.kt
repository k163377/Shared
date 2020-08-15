package com.mapk.core.internal

import kotlin.reflect.KParameter

internal class BucketGenerator private constructor(
    private val keyList: List<KParameter>,
    private val originalInitializationStatus: Array<Boolean>,
    private val originalValueArray: Array<Any?>
) {
    fun generate(): ArgumentBucket {
        return ArgumentBucket(
            keyList,
            originalValueArray.clone(),
            originalInitializationStatus.clone()
        )
    }

    companion object {
        fun of(keyList: List<KParameter>, instance: Any?): Pair<BucketGenerator, Int> {
            val capacity = keyList.size

            val originalInitializationStatus: Array<Boolean> = Array(capacity) { false }
            val originalValueArray: Array<Any?> = arrayOfNulls(capacity)

            val initialCount: Int = if (instance != null) {
                originalValueArray[0] = instance
                originalInitializationStatus[0] = true
                1
            } else {
                0
            }

            return BucketGenerator(keyList, originalInitializationStatus, originalValueArray) to initialCount
        }
    }
}
