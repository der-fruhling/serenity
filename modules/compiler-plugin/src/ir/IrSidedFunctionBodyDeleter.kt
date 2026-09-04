package net.derfruhling.serenity.compiler.ir

import net.derfruhling.serenity.compiler.Side
import net.derfruhling.serenity.compiler.clientClass
import net.derfruhling.serenity.compiler.serverClass
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.Scope
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrAnnotation
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.irConstructorCall
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.platform.NativePlatform
import org.jetbrains.kotlin.platform.PotentiallyWebPlatform
import org.jetbrains.kotlin.platform.jvm.JvmPlatform

class IrSidedFunctionBodyDeleter(private val context: IrPluginContext) : AbstractSerenityTransformer() {
    val expectedSide by lazy {
        var side = null as Side?

        for (p in context.platform ?: return@lazy null) {
            when (p) {
                is JvmPlatform, is NativePlatform -> {
                    if (side == Side.CLIENT) return@lazy null
                    side = Side.SERVER
                }

                is PotentiallyWebPlatform -> {
                    if (p.isWeb) {
                        if (side == Side.SERVER) return@lazy null
                        side = Side.CLIENT
                    } else {
                        if (side == Side.CLIENT) return@lazy null
                        side = Side.SERVER
                    }
                }
            }
        }

        side
    }

    val notImplementedErrorType by lazy {
        context.finderForBuiltins().findClass(ClassId.fromString(NotImplementedError::class.qualifiedName!!.replace('.', '/')))!!
            .typeWith()
    }

    val notImplementedError by lazy {
        context.finderForBuiltins().findConstructors(ClassId.fromString(NotImplementedError::class.qualifiedName!!.replace('.', '/')))
            .find { it.owner.parameters.isEmpty() }!!
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
        return when (annotation.classId) {
            clientClass -> Side.CLIENT
            serverClass -> Side.SERVER
            else -> null
        }
    }

    override fun visitFunction(declaration: IrFunction): IrStatement {
        if (expectedSide != null) {
            val targetSide = declaration.getSide()
            if (targetSide != null && expectedSide != targetSide) {
                declaration.body = IrBlockBodyBuilder(context, Scope(declaration.symbol), 0, 0).blockBody {
                    +irThrow(irConstructorCall(IrConstructorCallImpl(
                        UNDEFINED_OFFSET,
                        UNDEFINED_OFFSET,
                        notImplementedErrorType,
                        notImplementedError,
                        0,
                        0
                    ), notImplementedError))
                }
            }
        }

        return super.visitFunction(declaration)
    }
}