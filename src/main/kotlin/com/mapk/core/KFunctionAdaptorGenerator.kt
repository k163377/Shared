package com.mapk.core

import com.mapk.annotations.KParameterFlatten
import com.mapk.core.internal.BucketGenerator
import com.mapk.core.internal.ValueParameterGenerator
import com.mapk.core.internal.getKConstructor
import com.mapk.core.internal.isUseDefaultArgument
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible

class KFunctionAdaptorGenerator<T>(
    private val function: KFunction<T>,
    // TODO: parameterNameConverter: ParameterNameConverter
    instance: Any? = null,
    private val index: Int?
) {
    private val parameters: List<KParameter> = function.parameters
    private val valueParameterGenerators: List<ValueParameterGenerator>
    private val childGenerators: List<KFunctionAdaptorGenerator<*>>
    private val bucketGenerator: BucketGenerator = BucketGenerator(parameters, instance)

    init {
        if (parameters.isEmpty() || (instance != null && parameters.size == 1))
            throw IllegalArgumentException("This function is not require arguments.")

        // この関数には確実にアクセスするためアクセシビリティ書き換え
        function.isAccessible = true

        val tempValueParameters = ArrayList<ValueParameterGenerator>()
        val tempChildGenerators = ArrayList<KFunctionAdaptorGenerator<*>>()

        parameters.forEach { param ->
            if (param.kind == KParameter.Kind.VALUE && !param.isUseDefaultArgument()) {
                param.findAnnotation<KParameterFlatten>()
                    ?.let {
                        val (tempInstance, tempConstructor) = param.getKClass().getKConstructor()
                        tempChildGenerators.add(KFunctionAdaptorGenerator(tempConstructor, tempInstance, param.index))
                    }
                    ?: tempValueParameters.add(ValueParameterGenerator(param))
            }
        }

        valueParameterGenerators = tempValueParameters
        childGenerators = tempChildGenerators
    }

    fun generateAdaptor(): KFunctionAdaptor<T> {
        return KFunctionAdaptor(
            function,
            index,
            valueParameterGenerators,
            bucketGenerator.generate(),
            childGenerators.map { it.generateAdaptor() }
        )
    }
}
