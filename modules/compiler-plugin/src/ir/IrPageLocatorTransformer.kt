package net.derfruhling.serenity.compiler.ir

import net.derfruhling.serenity.compiler.Names
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.builders.IrSingleStatementBuilder
import org.jetbrains.kotlin.ir.builders.Scope
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionReference
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.callableId
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.isObject
import org.jetbrains.kotlin.ir.util.target
import org.jetbrains.kotlin.name.ClassId

class IrPageLocatorTransformer(private val context: IrPluginContext) : AbstractSerenityTransformer() {
    override fun visitCall(expression: IrCall): IrExpression {
        return when(expression.target.callableId) {
            Names.pageFactoryFinder -> {
                val target = (expression.arguments[0]!! as IrFunctionReference).symbol.owner
                val targetClassId = ClassId(target.callableId.packageName, target.callableId.callableName)
                val finder = context.finderForSource(currentFile)
                val actualClass = finder.findClass(targetClassId)
                    ?: return super.visitCall(expression)

                IrSingleStatementBuilder(context, Scope(expression.symbol), expression.startOffset, expression.endOffset).build {
                    if(actualClass.owner.isObject) {
                        irGetObject(actualClass)
                    } else {
                        irGetObject(actualClass.owner.companionObject()!!.symbol)
                    }
                }
            }
            Names.pageOfFinder -> super.visitCall(expression) // TODO
            else -> super.visitCall(expression)
        }
    }
}
