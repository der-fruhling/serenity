package net.derfruhling.serenity.compiler.ir

import net.derfruhling.serenity.compiler.Names
import net.derfruhling.serenity.compiler.Side
import org.jetbrains.kotlin.backend.common.extensions.DeclarationFinder
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.Scope
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrAnnotationImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrSimpleFunctionSymbolImpl
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name

class IrSidedFunctionBodyDeleter(private val context: IrPluginContext) : AbstractSerenityTransformer() {
    val expectedSide: Side? by lazy { Side.of(context.platform) }
    val finder: DeclarationFinder by lazy { context.finderForBuiltins() }

    val throwRemovedByOptimization by lazy {
        context.finderForBuiltins()
            .findFunctions(CallableId(serenityPackage, Name.identifier("removedByOptimization")))
            .single()
    }

    val stubClass by lazy {
        context.finderForBuiltins()
            .findClass(Names.stubClass)!!
    }

    val stubType by lazy { stubClass.typeWith() }

    val stubConstructor by lazy {
        context.finderForBuiltins()
            .findConstructors(Names.stubClass)
            .single()
    }

    private tailrec fun <T> T.getSide(): Side? where T : IrAnnotationContainer {
        annotations.firstNotNullOfOrNull {
            sideOf(it)
                ?: it.type.annotations.firstNotNullOfOrNull(::sideOf)
        }?.let { return it }

        return if (this !is IrDeclaration || parent !is IrAnnotationContainer) null
        else (parent as IrAnnotationContainer).getSide()
    }

    private fun sideOf(annotation: IrAnnotation): Side? {
        return when (annotation.classSymbol.owner.classId) {
            Names.clientClass -> Side.CLIENT
            Names.serverClass -> Side.SERVER
            else -> null
        }
    }

    private lateinit var currentFunction: IrFunction

    override fun visitFunction(declaration: IrFunction): IrStatement {
        if (expectedSide != null) {
            val targetSide = declaration.getSide()
            if (targetSide != null && expectedSide != targetSide) {
                rewriteFunction(declaration)
            }
        }

        currentFunction = declaration
        return super.visitFunction(declaration)
    }

    private fun rewriteFunction(declaration: IrFunction) {
        val annotation =
            IrAnnotationImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, stubType, stubConstructor, 0, 0)

        declaration.copyAnnotationsFrom(object : IrAnnotationContainer {
            override val annotations: List<IrAnnotation> = listOf(annotation)
        })

        declaration.body =
            IrBlockBodyBuilder(
                context,
                Scope(declaration.symbol),
                SYNTHETIC_OFFSET,
                SYNTHETIC_OFFSET
            ).blockBody {
                +IrCallImpl(
                    SYNTHETIC_OFFSET,
                    SYNTHETIC_OFFSET,
                    throwRemovedByOptimization.owner.returnType,
                    throwRemovedByOptimization
                )
            }
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val target = expression.target
        if(expectedSide != null && target.parameters.any { it.type.isFunction() }) {
            val call = super.visitCall(expression) as IrCall
            for(param in target.parameters.filter { it.type.isFunction() }) {
                val side = param.type.getSide() ?: continue
                val expr = call.arguments[param] ?: continue
                if(side != expectedSide) {
                    val funExpr = expr as? IrFunctionExpression
                        ?: run {
                            val targetFun = expr.type.getClass()!!.invokeFun!!
                            IrFunctionExpressionImpl(
                                SYNTHETIC_OFFSET,
                                SYNTHETIC_OFFSET,
                                param.type,
                                context.irFactory.createSimpleFunction(
                                    SYNTHETIC_OFFSET,
                                    SYNTHETIC_OFFSET,
                                    IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA,
                                    targetFun.name,
                                    DescriptorVisibilities.LOCAL,
                                    isInline = false,
                                    isExpect = false,
                                    targetFun.returnType,
                                    Modality.FINAL,
                                    IrSimpleFunctionSymbolImpl(),
                                    isTailrec = false,
                                    isSuspend = false,
                                    isOperator = false,
                                    isInfix = false
                                ),
                                IrStatementOrigin.LAMBDA
                            ).also {
                                call.arguments[param] = it
                                it.function.setDeclarationsParent(currentFunction)
                            }
                        }
                    val function = funExpr.function

                    if(!function.hasAnnotation(Names.stubClass)) {
                        rewriteFunction(function)
                    }
                }

                if(expr is IrFunctionExpression) {
                    expr.function.copyAnnotationsFrom(object : IrAnnotationContainer {
                        override val annotations: List<IrAnnotation> = listOf(side.createAnnotation(finder))
                    })
                }
            }

            return call
        }

        return super.visitCall(expression)
    }
}