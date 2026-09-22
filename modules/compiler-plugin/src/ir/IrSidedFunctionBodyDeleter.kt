package net.derfruhling.serenity.compiler.ir

import net.derfruhling.serenity.compiler.Names
import net.derfruhling.serenity.compiler.Side
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.Scope
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrAnnotation
import org.jetbrains.kotlin.ir.expressions.impl.IrAnnotationImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.constructedClass
import org.jetbrains.kotlin.ir.util.copyAnnotationsFrom
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name

class IrSidedFunctionBodyDeleter(private val context: IrPluginContext) : AbstractSerenityTransformer() {
    val expectedSide: Side? by lazy { Side.of(context.platform) }

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
        return when (annotation.symbol.owner.constructedClass.classId) {
            Names.clientClass -> Side.CLIENT
            Names.serverClass -> Side.SERVER
            else -> null
        }
    }

    override fun visitFunction(declaration: IrFunction): IrStatement {
        if (expectedSide != null) {
            val targetSide = declaration.getSide()
            if (targetSide != null && expectedSide != targetSide) {
                val annotation = IrAnnotationImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, stubType, stubConstructor, 0, 0)

                declaration.copyAnnotationsFrom(object : IrAnnotationContainer {
                    override val annotations: List<IrAnnotation> = listOf(annotation)
                })

                declaration.body =
                    IrBlockBodyBuilder(context, Scope(declaration.symbol), SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).blockBody {
                        +IrCallImpl(SYNTHETIC_OFFSET, SYNTHETIC_OFFSET, throwRemovedByOptimization.owner.returnType, throwRemovedByOptimization)
                    }
            }
        }

        return super.visitFunction(declaration)
    }
}