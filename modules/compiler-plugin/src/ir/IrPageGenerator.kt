package net.derfruhling.serenity.compiler.ir

import net.derfruhling.serenity.compiler.Names
import net.derfruhling.serenity.compiler.fir.FirPageGenerated
import net.derfruhling.serenity.compiler.hashFunctionName
import net.derfruhling.serenity.compiler.ir.components.PathStringInterpolator
import org.jetbrains.kotlin.backend.common.descriptors.synthesizedName
import org.jetbrains.kotlin.backend.common.extensions.DeclarationFinder
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.moveBodyTo
import org.jetbrains.kotlin.backend.common.lower.irNot
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.addProperty
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.createExpressionBody
import org.jetbrains.kotlin.ir.defaultType
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrSimpleFunctionSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.types.isNullableString
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.makeNotNull
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.types.typeWithArguments
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.JsStandardClassIds
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.platform.PotentiallyWebPlatform
import org.jetbrains.kotlin.resolve.JVM_NAME_ANNOTATION_FQ_NAME
import org.jetbrains.kotlin.types.Variance
import kotlin.collections.associateWith

class IrPageGenerator(context: IrPluginContext) : AbstractSerenityGenerator(context) {
    private val declarationOrigin = IrDeclarationOriginImpl("<generated>", true)

    private fun IrElementBuilder.synthetic() {
        startOffset = SYNTHETIC_OFFSET
        endOffset = SYNTHETIC_OFFSET
    }

    private lateinit var arguments: Map<IrValueParameter, IrProperty>
    private lateinit var params: Map<IrValueParameter, IrProperty?>

    private val jvmNameClass by lazy { context.finderForBuiltins().findClass(ClassId.topLevel(JVM_NAME_ANNOTATION_FQ_NAME))!! }
    private val jvmNameType by lazy { jvmNameClass.defaultType }
    private val jvmNameConstructor by lazy { jvmNameClass.constructors.single() }

    private fun jvmName(name: String): IrAnnotation = IrAnnotationImpl(
        SYNTHETIC_OFFSET,
        SYNTHETIC_OFFSET,
        jvmNameType,
        jvmNameConstructor,
        0,
        0,
    ).also {
        it.arguments[0] = name.toIrConst(context.irBuiltIns.stringType)
    }

    override fun visitClass(declaration: IrClass, data: FileFinder) {
        val origin = declaration.origin
        if(origin is IrDeclarationOrigin.GeneratedByPlugin) {
            val currentClassId = declaration.classId!!
            val ir = context.irFactory
            val finder = context.finderForSource(declaration.file)
            when(val key = origin.pluginKey) {
                is FirPageGenerated.PageClass -> {
                    val actualCallable = getActualCallable(currentClassId, declaration).owner
                    arguments = actualCallable.parameters.associateWith { arg ->
                        declaration.addProperty {
                            name = arg.name
                            this.origin = IrDeclarationOrigin.BRIDGE
                            synthetic()
                        }.also { prop ->
                            prop.addBackingField {
                                type = arg.type
                                isFinal = true
                            }.also { f ->
                                arg.defaultValue?.let { init ->
                                    f.initializer = init.deepCopyWithSymbols()
                                }
                            }

                            prop.addDefaultGetter(declaration, context.irBuiltIns)
                        }
                    }

                    val (descArgOrder, descOrder) = arguments.entries.map { it.toPair() }.unzip()
                    descriptorOrder = descOrder
                    descriptorArgumentOrder = descArgOrder

                    val (constructor, params) = if(!key.isSubClass) {
                        val constructor = declaration.addConstructor {
                            isPrimary = true
                            visibility = DescriptorVisibilities.PRIVATE
                            synthetic()
                        }

                        val params: Map<IrValueParameter, IrProperty> = emptyMap()

                        constructor to params
                    } else {
                        val constructor = declaration.primaryConstructor!!
                        val params = if (!declaration.isObject) {
                            constructor.parameters[0].defaultValue = ir.createExpressionBody(
                                IrGetObjectValueImpl(
                                    SYNTHETIC_OFFSET,
                                    SYNTHETIC_OFFSET,
                                    context.irBuiltIns.unitType,
                                    context.irBuiltIns.unitClass
                                )
                            )

                            val paramMap = constructor.parameters.associateBy { it.name }

                            arguments.values.associateBy { param ->
                                paramMap[param.name]!!
                            }
                        } else emptyMap()

                        constructor to params
                    }

                    super.visitClass(declaration, data)

                    val detailsProperty = declaration.properties.find { it.name == Name.identifier("details") }!!
                    val extensionsProperty = declaration.properties.find { it.name == Name.identifier("extensions") }!!
                    var contractFunction: IrFunction? = null
                    var thisParam: IrValueParameter? = null

                    actualCallable.transformChildrenVoid(object : IrElementTransformerVoid() {
                        private fun actuallyGetValue(expression: IrGetValue): IrExpression {
                            val parameter = expression.symbol.owner

                            return if (parameter is IrValueParameter) {
                                arguments[parameter]?.let { prop ->
                                    IrCallImpl(
                                        SYNTHETIC_OFFSET,
                                        SYNTHETIC_OFFSET,
                                        prop.getter!!.returnType,
                                        prop.getter!!.symbol
                                    ).also {
                                        it.arguments[0] = IrGetValueImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, thisParam!!.symbol)
                                    }
                                } ?: expression
                            } else expression
                        }

                        override fun visitCall(expression: IrCall): IrExpression {
                            val actualCallableId = try {
                                expression.target.callableId
                            } catch (_: IllegalStateException) {
                                null
                            }

                            if (actualCallableId == Names.pageContractFn) {
                                val lambda =
                                    expression.arguments.last()!! as IrFunctionExpression

                                contractFunction = context.irFactory.buildFun {
                                    updateFrom(lambda.function)
                                    name = Name.special("<page-contract>")
                                    returnType = lambda.function.returnType
                                    originalDeclaration = lambda.function
                                }.also { f ->
                                    f.annotations += jvmName($$"serenity$pageContract")
                                    f.copyParametersFrom(lambda.function)
                                    f.body = lambda.function.moveBodyTo(f)
                                    thisParam = f.addValueParameter($$"serenity$thisPage", declaration.defaultType)
                                    f.startOffset = UNDEFINED_OFFSET
                                    f.endOffset = UNDEFINED_OFFSET
                                    f.transformChildrenVoid(object : IrElementTransformerVoid() {
                                        private fun <T : IrElement> T.erase(): T {
                                            this.startOffset = UNDEFINED_OFFSET
                                            this.endOffset = UNDEFINED_OFFSET
                                            return this
                                        }

                                        override fun visitElement(element: IrElement): IrElement {
                                            return super.visitElement(element).erase()
                                        }

                                        override fun visitExpression(expression: IrExpression): IrExpression {
                                            return super.visitExpression(expression).erase()
                                        }

                                        override fun visitDeclaration(declaration: IrDeclarationBase): IrStatement {
                                            return super.visitDeclaration(declaration).erase()
                                        }

                                        override fun visitBody(body: IrBody): IrBody {
                                            return super.visitBody(body).erase()
                                        }

                                        override fun visitGetValue(expression: IrGetValue): IrExpression {
                                            expression.transformChildrenVoid()
                                            return actuallyGetValue(expression).erase()
                                        }
                                    })
                                }

                                return IrGetObjectValueImpl(
                                    SYNTHETIC_OFFSET,
                                    SYNTHETIC_OFFSET,
                                    context.irBuiltIns.unitType,
                                    context.irBuiltIns.unitClass
                                )
                            } else return super.visitCall(expression)
                        }
                    })

                    contractFunction?.let { fn ->
                        fn.visibility = DescriptorVisibilities.PRIVATE
                        declaration.addChild(fn)
                    }

                    constructor.body = IrBlockBodyBuilder(
                        context,
                        Scope(constructor.symbol),
                        SYNTHETIC_OFFSET,
                        SYNTHETIC_OFFSET
                    ).blockBody {
                        +irDelegatingConstructorCall(context.irBuiltIns.anyClass.constructors.single().owner)
                        +IrInstanceInitializerCallImpl(
                            SYNTHETIC_OFFSET,
                            SYNTHETIC_OFFSET,
                            declaration.symbol,
                            context.irBuiltIns.unitType
                        )

                        val thisVal = declaration.thisReceiver!!

                        if(params.isNotEmpty()) {
                            val path = actualCallable.getAnnotationArgumentValue<String>(Names.pageClass.asSingleFqName(), "path")!!

                            +irSetField(
                                irGet(thisVal),
                                declaration.properties.find { it.name == Name.identifier("path") }!!.backingField!!,
                                context(context.irBuiltIns) {
                                    PathStringInterpolator(params.keys.associate { it.name.asString() to irGet(it) })
                                        .interpolatePath(path)
                                }
                            )
                        }

                        if (contractFunction != null) {
                            val constructor =
                                finder.findConstructors(Names.pageContractImplClass)
                                    .single()
                            val contractBuilder = createTmpVariable(irCall(constructor))

                            val getDetails =
                                finder.findFunctions(Names.pageContractImplGetDetails)
                                    .single()
                            val getExtensions =
                                finder.findFunctions(Names.pageContractImplGetExtensions)
                                    .single()

                            +irCall(contractFunction).also { call ->
                                call.arguments[0] = irGet(contractBuilder)
                                call.arguments[1] = irGet(declaration.thisReceiver!!)
                            }

                            +irSetField(
                                irGet(thisVal),
                                detailsProperty.backingField!!,
                                irCall(getDetails).also { call ->
                                    call.arguments[0] = irGet(contractBuilder)
                                })

                            +irSetField(
                                irGet(thisVal),
                                extensionsProperty.backingField!!,
                                irCall(getExtensions).also { call ->
                                    call.arguments[0] = irGet(contractBuilder)
                                })
                        }

                        for ((valueParam, prop) in params) {
                            +irSetField(irGet(thisVal), prop.backingField!!, irGet(valueParam))
                        }
                    }
                }

                FirPageGenerated.PageFactoryClass -> {
                    val serialRegistryClass = finder.findClass(Names.serialRegistryClass)!!
                    val parserContextClass = finder.findClass(Names.parameterParserContextClass)!!
                    val parserClass = finder.findClass(Names.parameterParserClass)!!

                    val getParserFn = parserContextClass.getSimpleFunction("getParser")!!
                    val typeOfFn = finder.findFunctions(Names.typeOf).single()
                    val actualClass = declaration.nestedClasses.single()

                    context(context.irFactory) {
                        val parserContext = declaration.createProperty(
                            Name.identifier($$"$parserContext"),
                            parserContextClass.defaultType,
                            visibility = DescriptorVisibilities.PRIVATE_TO_THIS
                        ) {
                            irCall(
                                serialRegistryClass.getPropertyGetter("parameterParsers")!!,
                                type = parserContextClass.defaultType
                            ).also { call ->
                                call.arguments[0] =
                                    irGetObjectValue(serialRegistryClass.defaultType, serialRegistryClass)
                            }
                        }


                        val actualConstructor = actualClass.primaryConstructor!!
                        val arguments = actualConstructor.parameters.drop(1)

                        params = arguments.associateWith { arg ->
                            val actualType = arg.type.makeNotNull()
                            if(actualType.isString()) {
                                null
                            } else {
                                val parserType = parserClass.typeWith(actualType)
                                declaration.createProperty(
                                    Name.identifier($$"$${arg.name.asString()}$parser"),
                                    parserType
                                ) {
                                    irCallWithSubstitutedType(getParserFn, listOf(parserType)).also { call ->
                                        call.arguments[0] = irCall(parserContext.getter!!).also { call ->
                                            call.arguments[0] = irGet(declaration.thisReceiver!!)
                                        }
                                        call.arguments[1] = irCallWithSubstitutedType(typeOfFn, listOf(actualType))
                                    }
                                }
                            }
                        }
                    }

                    declaration.addSimpleDelegatingConstructor(context.irBuiltIns.anyClass.owner.primaryConstructor!!, context.irBuiltIns, isPrimary = true).also {
                        it.visibility = DescriptorVisibilities.PRIVATE
                    }

                    super.visitClass(declaration, data)
                }

                FirPageGenerated.PageSerializerClass -> {
                    declaration.addSimpleDelegatingConstructor(context.irBuiltIns.anyClass.constructors.single().owner, context.irBuiltIns, isPrimary = true).also {
                        it.visibility = DescriptorVisibilities.PRIVATE
                    }

                    context(ir, context.irBuiltIns, finder) {
                        declaration.generateStandardDecoder()
                    }

                    super.visitClass(declaration, data)
                }
            }
        } else super.visitClass(declaration, data)
    }

    private lateinit var descriptorOrder: List<IrProperty>
    private lateinit var descriptorArgumentOrder: List<IrValueParameter>

    override fun visitProperty(declaration: IrProperty, data: FileFinder) {
        val origin = declaration.origin
        super.visitProperty(declaration, data)

        if(origin is IrDeclarationOrigin.GeneratedByPlugin) {
            val currentClassId = declaration.parentAsClass.classId!!

            when(origin.pluginKey) {
                FirPageGenerated.PageIdProperty -> {
                    val id = hashFunctionName(currentClassId)
                    declaration.backingField!!.initializer = context.irFactory.createExpressionBody(id.toIrConst(context.irBuiltIns.stringType))
                }

                FirPageGenerated.PagePathProperty -> {
                    val actualCallable = getActualCallable(currentClassId, declaration)

                    val path = actualCallable.owner.getAnnotationArgumentValue<String>(Names.pageClass.asSingleFqName(), "path")!!
                    declaration.backingField!!.also {
                        if(declaration.parentAsClass.isObject) {
                            it.initializer = context.irFactory.createExpressionBody(path.toIrConst(context.irBuiltIns.stringType))
                        } else {
                            it.initializer = null
                            it.isFinal = false
                        }
                    }
                }

                FirPageGenerated.PageDetailsProperty -> context(context.irBuiltIns) {
                    val pageDetailsType by Names.pageDetailsClass.defaultType()
                    declaration.backingField!!.also {
                        it.initializer = null
                        it.isFinal = false
                    }
                }

                FirPageGenerated.PageExtensionsProperty -> context(context.irBuiltIns) {
                    val outAnnotation =
                        makeTypeProjection(context.irBuiltIns.annotationType, Variance.OUT_VARIANCE)
                    val extensionKClassType =
                        context.irBuiltIns.kClassClass.typeWithArguments(listOf(outAnnotation))
                    val abstractExtensionType by Names.abstractPageExtensionClass.defaultType()
                    val extensionMapType =
                        context.irBuiltIns.mapClass.typeWith(extensionKClassType, abstractExtensionType)

                    declaration.backingField!!.also {
                        it.initializer = null
                        it.isFinal = false
                    }
                }

                FirPageGenerated.PageDescriptorProperty -> context(context.irBuiltIns) {
                    val id = hashFunctionName(currentClassId)
                    val finder = context.finderForSource(declaration.file)

                    val targetClass = declaration.parentAsClass
                    val serialDescriptorBuilderClass = finder.findClass(Names.classSerialDescriptorBuilderClass)!!

                    val builderBlock = context.irFactory.createSimpleFunction(
                        SYNTHETIC_OFFSET,
                        SYNTHETIC_OFFSET,
                        IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA,
                        Name.identifier($$"serenity$serialDispatcherBlock"),
                        DescriptorVisibilities.LOCAL,
                        isInline = false,
                        isExpect = false,
                        context.irBuiltIns.unitType,
                        Modality.FINAL,
                        IrSimpleFunctionSymbolImpl(),
                        isTailrec = false,
                        isSuspend = false,
                        isOperator = false,
                        isInfix = false,
                        isExternal = false,
                    )
                    val builder = builderBlock.addValueParameter {
                        name = "receiver".synthesizedName
                        type = serialDescriptorBuilderClass.defaultType
                        kind = IrParameterKind.ExtensionReceiver
                        synthetic()
                    }

                    val elementFn by lazy { serialDescriptorBuilderClass.functionByName("element") }
                    val serializerFn by lazy {
                        finder.findFunctions(Names.serializerFun)
                            .find { it.owner.parameters.isEmpty() && it.owner.isInline }!!
                    }

                    val serializerClass by lazy {
                        finder.findClass(Names.kSerializerClass)!!
                    }

                    val getDescriptorFn by lazy {
                        serializerClass
                            .getPropertyGetter("descriptor")!!
                    }

                    builderBlock.body = IrBlockBodyBuilder(context, Scope(builderBlock.symbol), SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).blockBody {
                        for(value in descriptorOrder) {
                            +irCall(elementFn).also { call ->
                                call.arguments[0] = irGet(builder)
                                call.arguments[1] = value.name.asString().toIrConst(context.irBuiltIns.stringType)
                                call.arguments[2] = irCall(getDescriptorFn).also { call ->
                                    call.arguments[0] = irCall(serializerFn, serializerClass.typeWith(
                                        value.getter!!.returnType
                                    ), listOf(value.getter!!.returnType))
                                }
                            }
                        }
                    }

                    declaration.backingField!!.also {
                        val descriptorType = it.type
                        it.initializer = context.irFactory.createExpressionBody(
                            IrSingleStatementBuilder(context, Scope(declaration.symbol), SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).build {
                                irCall(finder.findFunctions(Names.buildClassSerialDescriptor).single()).also { call ->
                                    call.arguments[0] = id.toIrConst(context.irBuiltIns.stringType)
                                    call.arguments[1] = irCall(
                                        context.irBuiltIns.arrayOf,
                                        context.irBuiltIns.arrayClass.typeWith(descriptorType),
                                        listOf(descriptorType)
                                    )
                                    val superType = context.irBuiltIns.functionN(1)
                                    call.arguments[2] = IrFunctionExpressionImpl(
                                        SYNTHETIC_OFFSET,
                                        SYNTHETIC_OFFSET,
                                        IrSimpleTypeImpl(
                                            superType.symbol,
                                            false,
                                            listOf(serialDescriptorBuilderClass.defaultType, context.irBuiltIns.unitType),
                                            listOf(
                                                irAnnotation(finder.findClass(StandardClassIds.Annotations.ExtensionFunctionType)!!.owner.primaryConstructor!!.symbol)
                                            )
                                        ),
                                        builderBlock,
                                        IrStatementOrigin.LAMBDA
                                    ).also { builderBlock.parent = declaration.backingField!! }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun getActualCallable(
        currentClassId: ClassId,
        declaration: IrDeclaration
    ): IrSimpleFunctionSymbol {
        val actualClassId = currentClassId.outermostClassId
        val actualCallableId = CallableId(actualClassId.packageFqName, actualClassId.shortClassName)
        val actualCallable =
            context.finderForSource(declaration.file).findFunctions(actualCallableId).single {
                it.owner.hasAnnotation(Names.pageClass)
            }
        return actualCallable
    }

    override fun visitFunction(declaration: IrFunction, data: FileFinder) {
        val origin = declaration.origin
        super.visitFunction(declaration, data)

        val ir = context.irFactory
        if(origin is IrDeclarationOrigin.GeneratedByPlugin) context(ir) {
            val parentClass = declaration.parentClassOrNull
            val currentClassId = parentClass?.classIdOrFail
            val finder = context.finderForSource(declaration.file)
            when(val key = origin.pluginKey) {
                FirPageGenerated.PageMainFun -> {
                    val actualCallable = getActualCallable(currentClassId!!, declaration)
                    declaration.body = IrBlockBodyBuilder(context, Scope(declaration.symbol), SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).blockBody {
                        +irCall(actualCallable).also { call ->
                            actualCallable.owner.parameters.forEach { param ->
                                call.arguments[param] = irCall(arguments[param]!!.getter!!).also { call ->
                                    call.arguments[0] = irGet(declaration.dispatchReceiverParameter!!)
                                }
                            }
                        }
                    }
                }

                FirPageGenerated.PageFactoryCreateFun -> {
                    val parserClass = finder.findClass(Names.parameterParserClass)!!
                    val parseFn = parserClass.getSimpleFunction("parse")!!

                    val parentClass = parentClass

                    val valueParam = declaration.parameters.find { it.name == Name.identifier("ctx") }!!
                    val errorClass = finder.findClass(Names.illegalArgumentException)!!
                    val errorCon = errorClass.constructors.find { con ->
                        con.owner.parameters.size == 1 && (con.owner.parameters[0].type.isNullableString() || con.owner.parameters[0].type.isString())
                    }!!

                    val actualClass = parentClass!!.nestedClasses.single()
                    val actualConstructor = actualClass.primaryConstructor!!
                    val syntheticArgument = actualConstructor.parameters.first()
                    val arguments = actualConstructor.parameters.drop(1)

                    val pageContextType = finder.findClass(Names.pageContextClass)!!.owner

                    declaration.body =
                        IrBlockBodyBuilder(context, Scope(declaration.symbol), SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).blockBody {
                            +irReturn(irCall(actualConstructor).also { call ->
                                call.arguments[syntheticArgument] = irGetObject(context.irBuiltIns.unitClass)
                                val getParam = pageContextType.getSimpleFunction("getParameter")!!

                                for (arg in arguments) {
                                    call.arguments[arg] = irReturnableBlock(arg.type) {
                                        val value = createTmpVariable(irCall(getParam).also { call ->
                                            call.arguments[0] = irGet(valueParam)
                                            call.arguments[1] =
                                                arg.name.asString().toIrConst(context.irBuiltIns.stringType)
                                        }, irType = context.irBuiltIns.stringType.makeNullable())

                                        val nonNull = createTmpVariable(
                                            irIfNull(
                                                context.irBuiltIns.stringType,
                                                irGet(value),
                                                if (arg.type.isNullable()) {
                                                    IrReturnImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, context.irBuiltIns.nothingType, returnableBlockSymbol, irNull())
                                                } else {
                                                    irCall(errorCon).also { call ->
                                                        call.arguments[0] =
                                                            "No value provided for parameter '${arg.name.asString()}'".toIrConst(
                                                                context.irBuiltIns.stringType
                                                            )
                                                    }
                                                },
                                                irGet(value, type = context.irBuiltIns.stringType)
                                            ))

                                        val type = arg.type
                                        +IrReturnImpl(
                                            SYNTHETIC_OFFSET,
                                            SYNTHETIC_OFFSET,
                                            context.irBuiltIns.nothingType,
                                            returnableBlockSymbol,
                                            when {
                                                type.isString() -> irGet(nonNull)
                                                else -> irCall(parseFn, parseFn.owner.returnType.substitute(mapOf(
                                                    parserClass.owner.typeParameters.single().symbol to type
                                                ))).also { call ->
                                                    call.arguments[0] = irCall(params[arg]!!.getter!!).also { call ->
                                                        call.arguments[0] = irGetObject(parentClass.symbol)
                                                    }
                                                    call.arguments[1] =
                                                        arg.name.asString().toIrConst(context.irBuiltIns.stringType)
                                                    call.arguments[2] = irGet(nonNull)
                                                }
                                            }
                                        )
                                    }
                                }
                            })
                        }
                }

                FirPageGenerated.PageFactoryOfFun -> {
                    val actualClass = parentClass!!.nestedClasses.single()
                    val actualConstructor = actualClass.primaryConstructor!!
                    val syntheticArgument = actualConstructor.parameters.first()

                    declaration.body = IrBlockBodyBuilder(context, Scope(declaration.symbol), SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).blockBody {
                        +irReturn(irCall(actualConstructor).also {
                            it.arguments[syntheticArgument] = irGetObject(context.irBuiltIns.unitClass)

                            for(param in declaration.nonDispatchParameters) {
                                it.arguments[actualConstructor.parameters.find { par -> par.name == param.name }!!] = irGet(param)
                            }
                        })
                    }
                }

                FirPageGenerated.PageSerializerGetter -> {
                    parentClass!!

                    val parentOrigin = parentClass.origin
                    val targetClass = if(parentOrigin is IrDeclarationOrigin.GeneratedByPlugin && parentOrigin.pluginKey == FirPageGenerated.PageFactoryClass) {
                        parentClass.nestedClasses.single().nestedClasses.single()
                    } else parentClass.nestedClasses.single()

                    declaration.body = IrBlockBodyBuilder(context, Scope(declaration.symbol), SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).blockBody {
                        +irReturn(irGetObject(targetClass.symbol))
                    }
                }

                FirPageGenerated.PageSerializeFun -> {
                    val self = declaration.dispatchReceiverParameter!!
                    val (encoder, value) = declaration.nonDispatchParameters
                    val descriptor = parentClass!!.getPropertyGetter("descriptor")!!

                    val encoderClass = finder.findClass(Names.encoderClass)!!
                    val compositeEncoderClass = finder.findClass(Names.compositeEncoderClass)!!
                    val compositeEncoderEncodeValue = compositeEncoderClass.functionByName("encodeSerializableElement")
                    val compositeEncoderEnd = compositeEncoderClass.functionByName("endStructure")
                    val kSerializerClass = finder.findClass(Names.kSerializerClass)!!
                    val serializerFn = finder.findFunctions(Names.serializerFun).first {
                        it.owner.parameters.isEmpty() && it.owner.isInline
                    }

                    declaration.body = IrBlockBodyBuilder(context, Scope(declaration.symbol), SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).blockBody {
                        val descriptor = createTmpVariable(irCall(descriptor).also { call ->
                            call.arguments[0] = irGet(self)
                        }, "descriptor")

                        val compositeEncoder = createTmpVariable(irCall(encoderClass.functionByName("beginStructure")).also { call ->
                            call.arguments[0] = irGet(encoder)
                            call.arguments[1] = irGet(descriptor)
                        }, "compositeEncoder")

                        for((index, prop) in descriptorOrder.withIndex()) {
                            val targetType = prop.getter!!.returnType
                            +irCall(compositeEncoderEncodeValue).also { call ->
                                call.typeArguments[0] = targetType
                                call.arguments[0] = irGet(compositeEncoder)
                                call.arguments[1] = irGet(descriptor)
                                call.arguments[2] = index.toIrConst(context.irBuiltIns.intType)
                                call.arguments[3] = irCall(serializerFn, kSerializerClass.typeWith(targetType), listOf(targetType))
                                call.arguments[4] = irCall(prop.getter!!).also { call ->
                                    call.arguments[0] = irGet(value)
                                }
                            }
                        }

                        +irCall(compositeEncoderEnd).also { call ->
                            call.arguments[0] = irGet(compositeEncoder)
                            call.arguments[1] = irGet(descriptor)
                        }
                    }
                }

                FirPageGenerated.PageDeserializeFun -> {
                    val self = declaration.dispatchReceiverParameter!!
                    val (decoder) = declaration.nonDispatchParameters

                    val standardDecoder = parentClass!!.getSimpleFunction("deserializeNormally")!!

                    declaration.body = IrBlockBodyBuilder(context, Scope(declaration.symbol), SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).blockBody {
                        +irReturn(irCall(standardDecoder).also {
                            it.arguments[0] = irGet(self)
                            it.arguments[1] = irGet(decoder)
                        })
                    }
                }

                is FirPageGenerated.PageEntrypointFun if context.platform?.all { it is PotentiallyWebPlatform && it.isWeb } ?: false -> {
                    val actualCallable = finder.findFunctions(key.callableId).single {
                        it.owner.hasAnnotation(Names.pageClass)
                    }

                    val actualCallableId = actualCallable.owner.callableId
                    val rootClass = ClassId(actualCallableId.packageName, actualCallableId.callableName)
                    val actualClassId = rootClass.let {
                        if(actualCallable.owner.parameters.isNotEmpty()) {
                            it.createNestedClassId(Name.identifier("Instance"))
                        } else {
                            it
                        }
                    }

                    val actualClass = finder.findClass(actualClassId)!!
                    val id = hashFunctionName(actualClassId)

                    if(actualClass.owner.isObject) {
                        declaration.body = IrBlockBodyBuilder(context, Scope(declaration.symbol), SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).blockBody {
                            declaration.annotations += irAnnotation(finder.findConstructors(
                                JsStandardClassIds.Annotations.JsExport
                            ).single())

                            declaration.annotations += irAnnotation(finder.findConstructors(
                                JsStandardClassIds.Annotations.JsName
                            ).single()).also {
                                it.arguments[0] = irString(id)
                            }

                            +irCall(finder.findFunctions(Names.invokeCommonEntryPoint).single()).also { call ->
                                call.arguments[0] = irGetObject(actualClass)
                            }
                        }
                    } else {
                        val obj = declaration.parameters[0]

                        val serialRegistry = finder.findClass(Names.serialRegistryClass)!!
                        val decodeFromObject = finder.findFunctions(Names.decodeFromObjectExtension).single()

                        declaration.body = IrBlockBodyBuilder(context, Scope(declaration.symbol), SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).blockBody {
                            declaration.annotations += irAnnotation(finder.findConstructors(
                                JsStandardClassIds.Annotations.JsExport
                            ).single())

                            declaration.annotations += irAnnotation(finder.findConstructors(
                                JsStandardClassIds.Annotations.JsName
                            ).single()).also {
                                it.arguments[0] = irString(id)
                            }

                            +irCall(finder.findFunctions(Names.invokeCommonEntryPoint).single()).also { call ->
                                call.arguments[0] = irCall(decodeFromObject, actualClass.defaultType, listOf(actualClass.defaultType)).also { call ->
                                    call.arguments[0] = irGetObject(serialRegistry)
                                    call.arguments[1] = irGet(obj)
                                    val rootClass = finder.findClass(rootClass)!!
                                    call.arguments[2] = irCall(rootClass.functionByName("serializer")).also { call ->
                                        call.arguments[0] = irGetObject(rootClass)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    context(b: IrBuiltIns, ir: IrFactory, finder: DeclarationFinder)
    private fun IrClass.generateStandardDecoder(): IrFunction {
        val declaration = addFunction {
            name = Name.identifier("deserializeNormally")
            visibility = DescriptorVisibilities.PRIVATE_TO_THIS
            returnType = parentAsClass.defaultType
            synthetic()
        }
        val descriptor = this.getPropertyGetter("descriptor")!!

        val decoderClass = finder.findClass(Names.decoderClass)!!
        val self = declaration.addValueParameter {
            name = SpecialNames.THIS
            type = this@generateStandardDecoder.defaultType
            kind = IrParameterKind.DispatchReceiver
            synthetic()
        }
        val decoder = declaration.addValueParameter("decoder", decoderClass.defaultType)
        val compositeDecoderClass = finder.findClass(Names.compositeDecoderClass)!!
        val compositeDecoderDecodeIndex = compositeDecoderClass.functionByName("decodeElementIndex")
        val compositeDecoderDecodeValue = compositeDecoderClass.functionByName("decodeSerializableElement")
        val compositeDecoderDecodeNullableValue = compositeDecoderClass.functionByName("decodeNullableSerializableElement")
        val compositeDecoderEnd = compositeDecoderClass.functionByName("endStructure")
        val kSerializerClass = finder.findClass(Names.kSerializerClass)!!
        val serializerFn = finder.findFunctions(Names.serializerFun).first {
            it.owner.parameters.isEmpty() && it.owner.isInline
        }

        declaration.body = IrBlockBodyBuilder(context, Scope(declaration.symbol), SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).blockBody {
            val descriptor = createTmpVariable(irCall(descriptor).also { call ->
                call.arguments[0] = irGet(self)
            }, "descriptor")

            val compositeDecoder = createTmpVariable(irCall(decoderClass.functionByName("beginStructure")).also { call ->
                call.arguments[0] = irGet(decoder)
                call.arguments[1] = irGet(descriptor)
            }, "compositeEncoder")

            val values = descriptorArgumentOrder.map {
                createTmpVariable(irNull(), it.name.asString(), isMutable = true, irType = it.type.makeNullable()) to
                    createTmpVariable(irFalse(), it.name.asString() + "Set", isMutable = true, irType = context.irBuiltIns.booleanType)
            }

            val indexVal = createTmpVariable((-2).toIrConst(b.intType), isMutable = true, irType = b.intType)
            val branches = mutableListOf<IrBranch>()
            val argExprs = mutableListOf<IrExpression>()

            for((index, prop) in descriptorOrder.withIndex()) {
                val targetType = prop.getter!!.returnType
                val (value, isSet) = values[index]
                branches += irBranch(
                    irEquals(irGet(indexVal), index.toIrConst(context.irBuiltIns.intType)),
                    irBlock(resultType = context.irBuiltIns.unitType) {
                        +irSet(value, irCall(if(targetType.isNullable()) {
                            compositeDecoderDecodeNullableValue
                        } else {
                            compositeDecoderDecodeValue
                        }, targetType, listOf(targetType.makeNotNull())).also { call ->
                            call.arguments[0] = irGet(compositeDecoder)
                            call.arguments[1] = irGet(descriptor)
                            call.arguments[2] = index.toIrConst(context.irBuiltIns.intType)
                            call.arguments[3] = irCall(serializerFn, kSerializerClass.typeWith(targetType), listOf(targetType))
                            call.arguments[4] = irGet(value)
                        })

                        +irSet(isSet, irTrue())
                        +irUnit()
                    }
                )

                argExprs += irWhen(targetType, buildList {
                    if(prop.backingField?.initializer == null) {
                        add(irBranch(
                            irNot(irGet(isSet)),
                            irThrow(irCall(b.illegalArgumentExceptionSymbol).also { call ->
                                call.arguments[0] = irString("Property '${prop.name.asString()}' was not set during deserialization")
                            })
                        ))
                    }

                    if(!targetType.isNullable()) {
                        add(irBranch(
                            irEqualsNull(irGet(value)),
                            irThrow(irCall(b.illegalArgumentExceptionSymbol).also { call ->
                                call.arguments[0] = irString("Property '${prop.name.asString()}' has null value")
                            })
                        ))
                    }

                    add(irElseBranch(irCastIfNeeded(irGet(value), targetType)))
                })
            }

            val parentClass = parentAsClass
            +irDoWhile().also { loop ->
                loop.body = irBlock {
                    +irSet(indexVal, irCall(compositeDecoderDecodeIndex).also { call ->
                        call.arguments[0] = irGet(compositeDecoder)
                        call.arguments[1] = irGet(descriptor)
                    })

                    // decode done
                    branches += irBranch(irEquals(irGet(indexVal), (-1).toIrConst(b.intType)), irBreak(loop))

                    // unknown name
                    branches += irBranch(irEquals(irGet(indexVal), (-3).toIrConst(b.intType)), irContinue(loop))

                    // invalid index
                    branches += irElseBranch(irThrow(irCall(b.illegalArgumentExceptionSymbol).also { call ->
                        call.arguments[0] = irConcat().also {
                            it.arguments += irString("Invalid serial index: ")
                            it.arguments += irCall(b.intClass.functionByName("toString")).also { call ->
                                call.arguments[0] = irGet(indexVal)
                            }
                        }
                    }))

                    +irWhen(b.unitType, branches)
                }

                loop.condition = irNotEquals(irGet(indexVal), (-1).toIrConst(b.intType))
            }

            +irCall(compositeDecoderEnd).also { call ->
                call.arguments[0] = irGet(compositeDecoder)
                call.arguments[1] = irGet(descriptor)
            }

            +irReturn(if(parentClass.isObject) {
                irGetObject(parentClass.symbol)
            } else {
                val constructor = parentClass.constructors.single {
                    it.parameters[0].name == Name.identifier($$"serenity$identifier")
                }

                irCall(constructor).also { call ->
                    call.arguments[0] = irUnit()

                    for((i, arg) in argExprs.withIndex()) {
                        call.arguments[i + 1] = arg
                    }
                }
            })
        }

        return declaration
    }

    context(ir: IrFactory)
    fun <T : IrExpression> IrClass.createProperty(
        name: Name,
        type: IrType,
        visibility: DescriptorVisibility = DescriptorVisibilities.PUBLIC,
        fn: IrSingleStatementBuilder.() -> T
    ): IrProperty {
        return addProperty {
            modality = Modality.FINAL
            this.name = name
            this.visibility = visibility
            synthetic()
        }.also { p ->
            val field = p.addBackingField {
                this.type = type
                synthetic()
            }

            field.initializer = ir.createExpressionBody(
                IrSingleStatementBuilder(
                    context,
                    Scope(p.symbol),
                    SYNTHETIC_OFFSET,
                    SYNTHETIC_OFFSET
                ).build(fn)
            )

            p.addDefaultGetter(this, context.irBuiltIns)
        }
    }
}