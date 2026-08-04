package net.derfruhling.serenity.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSValueParameter
import net.derfruhling.serenity.annotations.UseParser

class PageParameters(function: KSFunctionDeclaration, val logger: KSPLogger) {
    val params = function.parameters.map(::Param)

    inner class Param(val parameter: KSValueParameter) {
        val name by lazy { parameter.name?.getShortName() }
        val propertyName by lazy { "_${name ?: "serenity_receiver"}" }
        val serialName by lazy { name ?: "_receiver" }
        val typeRef by parameter::type
        val type by lazy { parameter.type.resolve() }

        val annotation by lazy {
            fun isUseParser(a: KSAnnotation): Boolean =
                a.annotationType.resolve().declaration.qualifiedName!!.asString() == UseParser::class.qualifiedName!!

            parameter.annotations.find(::isUseParser)
                ?: type.annotations.find(::isUseParser)
        }

        val builtinParamParser by lazy {
            BuiltinParamParser.all[type.declaration.qualifiedName!!.asString()]
        }

        val parseExpr by lazy {
            if(annotation != null) {
                val a = annotation!!
                val type = a.arguments
                    .mapNotNull(KSValueArgument::value)
                    .filterIsInstance<KSType>()
                    .single()
                "${type.declaration.qualifiedName!!}.parse(it)"
            } else {
                builtinParamParser?.parse ?: run {
                    logger.error(
                        "No @UseParser annotation on type ${type.declaration.qualifiedName!!.asString()} and no default is defined",
                        parameter.type
                    )
                    "it"
                }
            }
        }
    }

    val paramMap = params.associateBy { it.serialName }

    private val paramRegex = Regex("""\{([^}.]+(?:\.\.\.)?)}""")

    fun pathExpression(path: String): String = path.replace(paramRegex) {
        $$"${$${paramMap[it.groupValues[1]]?.propertyName ?: it.groupValues[1]}}"
    }
}
