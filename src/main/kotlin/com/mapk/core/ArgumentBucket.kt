package com.mapk.core

import kotlin.reflect.KParameter

class ArgumentBucket private constructor(
    capacity: Int,
    private val initializeMask: List<Int>,
    private val completionValue: Int
) : MutableMap<KParameter, Any?> {
    private val keyArray: Array<KParameter?> = Array(capacity) { null }
    private val valueArray: Array<Any?> = Array(capacity) { null }

    private var count: Int = 0
    private var initializationStatus: Int = 0

    val isInitialized: Boolean get() = initializationStatus == completionValue

    class MutableEntry internal constructor(
        override val key: KParameter,
        override var value: Any?
    ) : MutableMap.MutableEntry<KParameter, Any?> {
        override fun setValue(newValue: Any?): Any? {
            throw UnsupportedOperationException()
        }
    }

    override val size: Int get() = count

    override fun containsKey(key: KParameter): Boolean {
        // NOTE: もしかしたらステータスを見た方が速いかも
        return keyArray[key.index] != null
    }

    override fun containsValue(value: Any?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun get(key: KParameter): Any? = valueArray[key.index]

    override fun isEmpty(): Boolean = count == 0

    override val entries: MutableSet<MutableMap.MutableEntry<KParameter, Any?>>
        get() = keyArray.mapNotNull { it?.let { MutableEntry(it, valueArray[it.index]) } }.toMutableSet()
    override val keys: MutableSet<KParameter>
        get() = keyArray.filterNotNull().toMutableSet()
    override val values: MutableCollection<Any?>
        get() = if (isInitialized) values.toMutableList()
            else throw UnsupportedOperationException("Must be full initialize.")

    override fun clear() {
        throw UnsupportedOperationException()
    }

    override fun put(key: KParameter, value: Any?) {
        val index = key.index
        val temp = initializationStatus or initializeMask[index]

        // 先に入ったものを優先するため、初期化済みなら何もしない
        if (initializationStatus == temp) return

        count += 1
        initializationStatus = temp
        keyArray[index] = key
        valueArray[index] = value

        return
    }

    override fun putAll(from: Map<out KParameter, Any?>) {
        throw UnsupportedOperationException()
    }

    override fun remove(key: KParameter): Any? {
        throw UnsupportedOperationException()
    }
}
