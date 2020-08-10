package com.mapk.core

import com.mapk.core.internal.BucketGenerator
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.isAccessible

class KFunctionAdaptorGenerator<T>(
    private val function: KFunction<T>,
    // TODO: parameterNameConverter: ParameterNameConverter
    instance: Any? = null,
    private val index: Int?
) {
    private val parameters: List<KParameter> = function.parameters
    private val valueParameters: List<KParameter> = TODO()
    private val childGenerators: List<KFunctionAdaptorGenerator<*>> = TODO()
    private val bucketGenerator: BucketGenerator = BucketGenerator(parameters, instance)

    init {
        if (parameters.isEmpty() || (instance != null && parameters.size == 1))
            throw IllegalArgumentException("This function is not require arguments.")

        // この関数には確実にアクセスするためアクセシビリティ書き換え
        function.isAccessible = true
    }

    fun generateAdaptor(): KFunctionAdaptor<T> {
        return KFunctionAdaptor(
            function,
            index,
            valueParameters,
            bucketGenerator.generate(),
            childGenerators.map { it.generateAdaptor() }
        )
    }
}
