package com.mapk.core

import com.mapk.core.internal.BucketGenerator
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

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
