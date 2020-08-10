package com.mapk.core.internal

import com.mapk.annotations.KParameterRequireNonNull
import com.mapk.core.ValueParameter
import com.mapk.core.getKClass
import kotlin.reflect.KParameter

internal class ValueParameterGenerator(private val parameter: KParameter) {
    private val annotations = parameter.annotations
    private val index: Int = parameter.index
    private val isOptional: Boolean = parameter.isOptional

    private val isNullable: Boolean = annotations.none { it is KParameterRequireNonNull }
    private val name: String = parameter.getAliasOrName()!!
    private val requiredClazz = parameter.getKClass()

    fun generate(bucket: ArgumentBucket): ValueParameter<*> = ValueParameter(
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
