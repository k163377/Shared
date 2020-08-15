package com.mapk.core.internal

import kotlin.reflect.KParameter

internal class ArgumentBucket(
    private val keyList: List<KParameter>,
    internal val valueArray: Array<Any?>,
    val initializationStatuses: Array<Boolean>
) : Map<KParameter, Any?> {
    class Entry internal constructor(
        override val key: KParameter,
        override var value: Any?
    ) : Map.Entry<KParameter, Any?>

    // put状況の管理はAdaptorの方で行うため、ここではforcePutのみ提供
    fun forcePut(index: Int, value: Any?) {
        valueArray[index] = value
        initializationStatuses[index] = true
    }

    override val entries: Set<Map.Entry<KParameter, Any?>>
        get() = initializationStatuses.mapIndexedNotNull { index, isInitialized ->
            if (isInitialized) {
                Entry(keyList[index], valueArray[index])
            } else null
        }.toSet()
    override val keys: Set<KParameter>
        get() = keyList.filterIsInitialized().toSet()
    override val size: Int
        get() = initializationStatuses.count { it }
    override val values: Collection<Any?>
        get() = values.filterIsInitialized()

    override fun containsKey(key: KParameter): Boolean = initializationStatuses[key.index]
    override fun containsValue(value: Any?): Boolean = valueArray.any { it == value }
    override fun get(key: KParameter): Any? = valueArray[key.index]
    override fun isEmpty(): Boolean = size != 0

    // 初期化されているインデックスの内容だけ取り出す共通関数
    private fun<T> Collection<T>.filterIsInitialized() =
        filterIndexed { index, _ -> initializationStatuses[index] }
}
