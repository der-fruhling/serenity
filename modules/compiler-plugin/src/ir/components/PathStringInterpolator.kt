package net.derfruhling.serenity.compiler.ir.components

import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.makeNotNull
import org.jetbrains.kotlin.ir.util.isNullable

class PathStringInterpolator(val arguments: Map<String, IrExpression>) {
    context(ir: IrBlockBodyBuilder, builtin: IrBuiltIns)
    fun interpolatePath(path: String): IrExpression = with(ir) {
        if(arguments.isEmpty()) {
            irString(path)
        } else {
            ir.irConcat().also {
                var p = path

                if(p.startsWith('/')) {
                    p = p.substring(1)
                }

                for(segment in p.split('/')) {
                    it.arguments += irString("/")
                    if(segment.startsWith('{') && segment.endsWith('}')) {
                        val name = segment.removeSurrounding("{", "}")
                        val arg = arguments[name] ?: throw IllegalStateException("Argument $name not available")

                        it.arguments += if(arg.type.isString()) {
                            arg
                        } else {
                            val toString = arg.type.classOrFail.functionByName("toString")
                            if(arg.type.isNullable()) {
                                val value = createTmpVariable(arg, name)
                                irIfNull(
                                    builtin.stringType,
                                    irGet(value),
                                    irString("null"),
                                    irCall(toString).also { call ->
                                        call.arguments[0] = irGet(value, arg.type.makeNotNull())
                                    }
                                )
                            } else {
                                irCall(toString).also { call ->
                                    call.arguments[0] = arg
                                }
                            }
                        }
                    } else {
                        it.arguments += irString(segment)
                    }
                }
            }
        }
    }
}