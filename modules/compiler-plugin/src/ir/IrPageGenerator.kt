package net.derfruhling.serenity.compiler.ir

import net.derfruhling.serenity.compiler.Names
import net.derfruhling.serenity.compiler.fir.FirPageGenerated
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.moveBodyTo
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Modality
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
import org.jetbrains.kotlin.ir.descriptors.toIrBasedKotlinType
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrClassSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.SimpleTypeNullability
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.impl.IrCapturedType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeBuilder
import org.jetbrains.kotlin.ir.types.impl.buildSimpleType
import org.jetbrains.kotlin.ir.types.impl.buildTypeProjection
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.types.impl.toBuilder
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
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.JVM_NAME_ANNOTATION_FQ_NAME
import org.jetbrains.kotlin.resolve.calls.inference.extractTypeForGivenRecursiveTypeParameter
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.types.model.CaptureStatus
import java.security.MessageDigest
import kotlin.collections.associateWith
import kotlin.io.encoding.Base64
import kotlin.properties.Delegates

class IrPageGenerator(context: IrPluginContext) : AbstractSerenityGenerator(context) {
    private val declarationOrigin = IrDeclarationOriginImpl("<generated>", true)

    private fun IrElementBuilder.synthetic() {
        startOffset = SYNTHETIC_OFFSET
        endOffset = SYNTHETIC_OFFSET
    }

    private fun hashFunctionName(string: String): String {
        val digest = MessageDigest.getInstance("MD5")
        return Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
            .encode(digest.digest(string.toByteArray()))
    }

    private lateinit var arguments: Map<IrValueParameter, IrProperty>
    private lateinit var params: Map<IrValueParameter, IrProperty>

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

                        val actualClass = declaration.nestedClasses.single()
                        val actualConstructor = actualClass.primaryConstructor!!
                        val arguments = actualConstructor.parameters.drop(1)

                        params = arguments.associateWith { arg ->
                            val actualType = arg.type.makeNotNull()
                            val parserType = parserClass.typeWith(actualType)
                            declaration.createProperty(
                                Name.identifier($$"$${arg.name.asString()}$parser"),
                                parserType
                            ) {
                                irCallWithSubstitutedType(getParserFn, listOf(parserType)).also { call ->
                                    call.arguments[0] = irCall(parserContext.getter!!)
                                    call.arguments[1] = irCallWithSubstitutedType(typeOfFn, listOf(actualType))
                                }
                            }
                        }
                    }

                    declaration.addSimpleDelegatingConstructor(context.irBuiltIns.anyClass.owner.primaryConstructor!!, context.irBuiltIns, isPrimary = true).also {
                        it.visibility = DescriptorVisibilities.PRIVATE
                    }

                    super.visitClass(declaration, data)
                }
            }
        } else super.visitClass(declaration, data)
    }

    override fun visitProperty(declaration: IrProperty, data: FileFinder) {
        val origin = declaration.origin
        super.visitProperty(declaration, data)

        if(origin is IrDeclarationOrigin.GeneratedByPlugin) {
            val currentClassId = declaration.parentAsClass.classId!!

            when(origin.pluginKey) {
                FirPageGenerated.PageIdProperty -> {
                    val id = hashFunctionName(currentClassId.asFqNameString())
                    declaration.backingField!!.initializer = context.irFactory.createExpressionBody(id.toIrConst(context.irBuiltIns.stringType))
                }

                FirPageGenerated.PagePathProperty -> {
                    val actualCallable = getActualCallable(currentClassId, declaration)

                    // TODO string interpolation
                    val path = actualCallable.owner.getAnnotationArgumentValue<String>(Names.pageClass.asSingleFqName(), "path")!!
                    declaration.backingField!!.initializer = context.irFactory.createExpressionBody(path.toIrConst(context.irBuiltIns.stringType))
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
            val currentClassId = declaration.parentAsClass.classId!!
            val finder = context.finderForSource(declaration.file)
            when(origin.pluginKey) {
                FirPageGenerated.PageMainFun -> {
                    val actualCallable = getActualCallable(currentClassId, declaration)
                    declaration.body = IrBlockBodyBuilder(context, Scope(declaration.symbol), SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).blockBody {
                        +irCall(actualCallable).also { call ->
                            actualCallable.owner.parameters.forEach { param ->
                                call.arguments[param] = irCall(arguments[param]!!.getter!!)
                            }
                        }
                    }
                }

                FirPageGenerated.PageFactoryCreateFun -> {
                    val parserClass = finder.findClass(Names.parameterParserClass)!!
                    val parseFn = parserClass.getSimpleFunction("parse")!!

                    val parentClass = declaration.parentAsClass

                    val valueParam = declaration.parameters.find { it.name == Name.identifier("ctx") }!!
                    val errorClass = finder.findClass(Names.illegalArgumentException)!!
                    val errorCon = errorClass.constructors.find { con ->
                        con.owner.parameters.size == 1 && (con.owner.parameters[0].type.isNullableString() || con.owner.parameters[0].type.isString())
                    }!!

                    val actualClass = parentClass.nestedClasses.single()
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
                                                    irReturn(irNull())
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
                                        +irReturn(
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
                                            })
                                    }
                                }
                            })
                        }
                }

                FirPageGenerated.PageFactoryOfFun -> {
                    val actualClass = declaration.parentAsClass.nestedClasses.single()
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
            }
        }
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