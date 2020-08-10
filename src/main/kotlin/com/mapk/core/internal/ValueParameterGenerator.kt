package com.mapk.core.internal

import com.mapk.annotations.KParameterRequireNonNull
import com.mapk.core.ValueParameter
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

internal class ValueParameterGenerator<T : Any>(
    private val parameter: KParameter,
    private val name: String,
    private val requiredClazz: KClass<T>
) {
    private val annotations = parameter.annotations
    private val index: Int = parameter.index
    private val isOptional: Boolean = parameter.isOptional

    private val isNullable: Boolean = annotations.none { it is KParameterRequireNonNull }

    fun generate(bucket: ArgumentBucket): ValueParameter<T> = ValueParameter(
        annotations = annotations,
        index = index,
        isOptional = isOptional,
        isNullable = isNullable,
        name = name,
        requiredClazz = requiredClazz,
        parameter = parameter,
        bucket = bucket
    )
}
