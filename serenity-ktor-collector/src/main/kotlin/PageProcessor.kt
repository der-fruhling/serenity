package net.derfruhling.serenity.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Variance
import com.google.devtools.ksp.validate
import net.derfruhling.serenity.elements.Page

class PageProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
    options: Map<String, String>
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation("net.derfruhling.serenity.elements.Page")
            .filter { it.validate() }
            .filterIsInstance<KSFunctionDeclaration>()
            .toList()
            .forEach { it.accept(Acceptor(), Unit) }

        return emptyList()
    }

    inner class Acceptor : KSVisitorVoid() {
        @OptIn(KspExperimental::class)
        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            codeGenerator.createNewFile(
                Dependencies(false, function.containingFile!!),
                function.packageName.asString(),
                function.simpleName.asString() + ".generated"
            ).bufferedWriter().use { out ->
                if (function.packageName.asString().isNotEmpty()) out.append("package ${function.packageName.asString()}\n\n")
                out.appendLine("import androidx.compose.runtime.Composable")
                out.appendLine("import androidx.compose.runtime.key")
                out.appendLine("import androidx.compose.runtime.SideEffect")
                out.appendLine("import io.ktor.server.application.ApplicationCall")
                out.appendLine("import net.derfruhling.serenity.ktor.server.respondCompose")
                out.appendLine("import net.derfruhling.serenity.ktor.server.pageFunctionName")
                out.appendLine("import net.derfruhling.serenity.ktor.server.currentCall")
                out.appendLine("import net.derfruhling.serenity.PageHolder")
                out.appendLine("import net.derfruhling.serenity.annotations.HtmlComposable")
                out.appendLine("import kotlinx.serialization.Serializable")
                out.appendLine("import kotlinx.serialization.SerialName")

                out.appendLine()
                out.appendLine("@Serializable")
                out.appendLine("@SerialName(\"${hashFunctionName(function.qualifiedName!!.asString())}\")")
                out.appendLine("actual object ${function.simpleName.asString()} : PageHolder {")

                if (!function.annotations.any { it.annotationType.resolve().declaration.qualifiedName!!.asString() == "androidx.compose.runtime.Composable" }) {
                    logger.error("Pages must be composable", function)
                }
                val hashFunctionName = hashFunctionName(function.qualifiedName!!.asString())
                val annotation = function.getAnnotationsByType(Page::class).single()

                out.appendLine("""
                    actual override val id: String = "$hashFunctionName";
                    actual override val path: String = "${annotation.path}";
                """.trimIndent().prependIndent("    "))

                out.appendLine(
                    """
                    @Composable
                    @HtmlComposable
                    actual override fun Main() {
                        val _call: ApplicationCall = currentCall
                """.trimIndent().prependIndent("    ")
                )

                out.appendLine("        SideEffect { _call.attributes[pageFunctionName] = \"$hashFunctionName\" }")

                if (function.parameters.isNotEmpty()) {
                    for (it in function.parameters) {
                        out.appendLine("val _${it.name!!.asString()}: ${printType(it.type)} by _call.pathParameters")
                        out.appendLine("val ${it.name!!.asString()}: ${printType(it.type)} = rememberSerializable { _${it.name!!.asString()} }")
                    }

                    val paramNames = function.parameters.joinToString { it.name!!.asString() }
                    out.appendLine(
                        """ 
                        key($paramNames) {
                            ${function.qualifiedName!!.asString()}($paramNames)
                        }
                    """.trimIndent().prependIndent("        ")
                    )
                } else {
                    out.appendLine(
                        """
                        ${function.qualifiedName!!.asString()}()
                    """.trimIndent().prependIndent("        ")
                    )
                }

                out.appendLine("    }")
                out.appendLine('}')
            }
        }
    }

    private fun printType(typeReference: KSTypeReference): String = buildString {
        val element = typeReference.element
        if (element != null) {
            val type = typeReference.resolve()
            append(type.declaration.qualifiedName!!.asString())

            element.typeArguments.joinToString {
                when (it.variance) {
                    Variance.STAR -> {
                        return@joinToString "*"
                    }

                    Variance.INVARIANT -> ""
                    Variance.COVARIANT -> "in "
                    Variance.CONTRAVARIANT -> "out "
                } + printType(it.type!!)
            }
        }
    }

    class Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return PageProcessor(environment.codeGenerator, environment.logger, environment.options)
        }
    }
}