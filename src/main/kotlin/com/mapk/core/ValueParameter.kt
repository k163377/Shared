package com.mapk.core

import com.mapk.annotations.KParameterRequireNonNull
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

class ValueParameter<T : Any> private constructor(
    val annotations: List<Annotation>,
    internal val index: Int,
    val isOptional: Boolean,
    val isNullable: Boolean,
    val name: String,
    val requiredClazz: KClass<T>
) {
    internal constructor(
        parameter: KParameter,
        name: String,
        requiredClazz: KClass<T>
    ) : this(
        parameter.annotations,
        parameter.index,
        parameter.isOptional,
        parameter.annotations.none { it is KParameterRequireNonNull },
        name,
        requiredClazz
    )
}
