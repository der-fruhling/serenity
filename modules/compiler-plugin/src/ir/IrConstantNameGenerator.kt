package net.derfruhling.serenity.compiler.ir

import net.openhft.hashing.LongHashFunction
import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.ModuleLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.isTopLevelInPackage
import org.jetbrains.kotlin.ir.util.toIrConst
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.FqName

class IrConstantNameGenerator(private val context: IrPluginContext) : AbstractSerenityTransformer() {
    private val xx3 by lazy { LongHashFunction.xx3() }

    override fun visitCall(expression: IrCall): IrExpression {
        val sym = expression.symbol.owner
        if (sym.isTopLevelInPackage("n", serenityPackage)) {
            val string = expression.arguments[0] as IrConst
            return xx3.hashBytes((string.value as String).toByteArray())
                .toIrConst(context.irBuiltIns.longType)
        } else {
            return super.visitCall(expression)
        }
    }
}