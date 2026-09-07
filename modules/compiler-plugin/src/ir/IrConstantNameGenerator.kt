package net.derfruhling.serenity.compiler.ir

import net.derfruhling.serenity.compiler.NotApplicable
import net.derfruhling.serenity.compiler.SerenityWarnings
import net.openhft.hashing.LongHashFunction
import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.ModuleLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.unboxInlineClass
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.classSymbol
import org.jetbrains.kotlin.ir.declarations.IrDeclarationBase
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.defaultType
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.implicitCastTo
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultConstructor
import org.jetbrains.kotlin.ir.util.isTopLevelInPackage
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.toIrConst
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class IrConstantNameGenerator(private val context: IrPluginContext) : AbstractSerenityTransformer() {
    private val xx3 by lazy { LongHashFunction.xx3() }
    private val textType = context(context.irBuiltIns) {
        try {
            ClassId(localizationPackage, Name.identifier("ConstantName")).classSymbol()
        } catch(_: IllegalStateException) {
            throw NotApplicable()
        }
    }

    private val textConstructor = textType.owner.constructors.find {
        it.parameters.size == 1 && it.parameters[0].type == context.irBuiltIns.longType
    }!!

    private lateinit var currentDeclaration: IrDeclarationBase

    override fun visitDeclaration(declaration: IrDeclarationBase): IrStatement {
        currentDeclaration = declaration
        return super.visitDeclaration(declaration)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val sym = expression.symbol.owner
        if (sym.isTopLevelInPackage("n", localizationPackage)) {
            val string = expression.arguments[0] as? IrConst
                ?: return super.visitCall(expression)
            return IrConstructorCallImpl(
                SYNTHETIC_OFFSET,
                SYNTHETIC_OFFSET,
                textType.defaultType,
                textConstructor.symbol,
                0,
                0
            ).also {
                val hash = xx3.hashBytes((string.value as String).toByteArray())
                context.diagnosticReporter.at(it, currentDeclaration)
                    .report(SerenityWarnings.HELP_CONSTANT_NAME, string.value as String, hash)
                it.arguments[0] = hash
                    .toIrConst(textType.typeWith().unboxInlineClass())
            }
        } else {
            return super.visitCall(expression)
        }
    }
}