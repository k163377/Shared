package com.mapk.core

import com.mapk.annotations.KParameterFlatten
import com.mapk.core.internal.BucketGenerator
import com.mapk.core.internal.ParameterNameConverter
import com.mapk.core.internal.ValueParameterGenerator
import com.mapk.core.internal.getAliasOrName
import com.mapk.core.internal.getKConstructor
import com.mapk.core.internal.isUseDefaultArgument
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible

class KFunctionAdaptorGenerator<T> internal constructor(
    private val function: KFunction<T>,
    parameterNameConverter: ParameterNameConverter,
    instance: Any? = null,
    private val index: Int?
) {
    // 外部からの呼び出し用、外から呼ばれる = ルートなのでindexは無い
    constructor(function: KFunction<T>, parameterNameConverter: ((String) -> String)?, instance: Any?) : this(
        function,
        ParameterNameConverter.Simple(parameterNameConverter),
        instance,
        null
    )

    private val parameters: List<KParameter> = function.parameters
    private val valueParameterGenerators: List<ValueParameterGenerator<*>>
    private val childGenerators: List<KFunctionAdaptorGenerator<*>>
    private val bucketGenerator: BucketGenerator = BucketGenerator(parameters, instance)

    init {
        if (parameters.isEmpty() || (instance != null && parameters.size == 1))
            throw IllegalArgumentException("This function is not require arguments.")

        // この関数には確実にアクセスするためアクセシビリティ書き換え
        function.isAccessible = true

        val tempValueParameters = ArrayList<ValueParameterGenerator<*>>()
        val tempChildGenerators = ArrayList<KFunctionAdaptorGenerator<*>>()

        parameters.forEach { param ->
            val requiredClazz = param.getKClass()
            val name = param.getAliasOrName()!!

            if (param.kind == KParameter.Kind.VALUE && !param.isUseDefaultArgument()) {
                param.findAnnotation<KParameterFlatten>()
                    ?.let {
                        // 名前の変換処理
                        val converter: ParameterNameConverter = if (it.fieldNameToPrefix) {
                            // 結合が必要な場合は結合機能のインスタンスを持ってきて対応する
                            parameterNameConverter.nest(name, it.nameJoiner.objectInstance!!)
                        } else {
                            // プレフィックスを要求しない場合は全てsimpleでマップするように修正
                            parameterNameConverter.toSimple()
                        }

                        val (tempInstance, tempConstructor) = param.getKClass().getKConstructor()
                        tempChildGenerators.add(KFunctionAdaptorGenerator(
                            tempConstructor, converter, tempInstance, param.index
                        ))
                    }
                    ?: tempValueParameters.add(
                        ValueParameterGenerator(param, parameterNameConverter.convert(name), requiredClazz)
                    )
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

fun <T : Any> KClass<T>.toKFunctionAdaptorGenerator(
    parameterNameConverter: ((String) -> String)?
): KFunctionAdaptorGenerator<T> = this.getKConstructor().let { (instance, function) ->
    KFunctionAdaptorGenerator(function, parameterNameConverter, instance)
}
