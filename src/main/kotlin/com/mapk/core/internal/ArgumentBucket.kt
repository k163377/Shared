package com.mapk.core.internal

import kotlin.reflect.KParameter

internal class ArgumentBucket(
    private val keyList: List<KParameter>,
    internal val valueArray: Array<Any?>,
    private val initializationStatuses: Array<Boolean>,
    private var count: Int
) : Map<KParameter, Any?> {
    class Entry internal constructor(
        override val key: KParameter,
        override var value: Any?
    ) : Map.Entry<KParameter, Any?>

    val isFullInitialized: Boolean
        get() = count == keyList.size

    // Note: ここでifするのって妥当？n重チェックになってしまう気がするのでチェック場所を考える
    fun putIfAbsent(index: Int, value: Any?) {
        if (!initializationStatuses[index]) {
            valueArray[index] = value
            initializationStatuses[index] = true
            count++
        }
    }

    override val entries: Set<Map.Entry<KParameter, Any?>>
        get() = initializationStatuses.mapIndexedNotNull { index, isInitialized ->
            if (isInitialized) {
                Entry(keyList[index], valueArray[index])
            } else null
        }.toSet()
    override val keys: Set<KParameter>
        get() = keyList.filterIndexed { index, _ -> initializationStatuses[index] }.toSet()
    override val size: Int
        get() = count
    override val values: Collection<Any?>
        get() = values.filterIndexed { index, _ -> initializationStatuses[index] }

    override fun containsKey(key: KParameter): Boolean = initializationStatuses[key.index]
    override fun containsValue(value: Any?): Boolean = valueArray.any { it == value }
    override fun get(key: KParameter): Any? = valueArray[key.index]
    override fun isEmpty(): Boolean = count == 0
}
