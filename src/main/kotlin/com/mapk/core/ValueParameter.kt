package com.mapk.core

import com.mapk.core.internal.ArgumentBucket
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

class ValueParameter<T : Any> internal constructor(
    val annotations: List<Annotation>,
    val index: Int,
    val isOptional: Boolean,
    val isNullable: Boolean,
    val name: String,
    val requiredClazz: KClass<T>,
    internal val parameter: KParameter,
    internal val bucket: ArgumentBucket
)
