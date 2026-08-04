package net.derfruhling.serenity.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import net.derfruhling.serenity.annotations.RegisterPage

class PageProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
    options: Map<String, String>
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(RegisterPage::class.qualifiedName!!)
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
                if (function.packageName.asString()
                        .isNotEmpty()
                ) out.append("package ${function.packageName.asString()}\n\n")
                out.appendLine("import androidx.compose.runtime.Composable")
                out.appendLine("import androidx.compose.runtime.key")
                out.appendLine("import androidx.compose.runtime.SideEffect")
                out.appendLine("import io.ktor.server.application.ApplicationCall")
                out.appendLine("import net.derfruhling.serenity.ktor.server.respondCompose")
                out.appendLine("import net.derfruhling.serenity.ktor.server.pageFunctionName")
                out.appendLine("import net.derfruhling.serenity.ktor.server.currentCall")
                out.appendLine("import net.derfruhling.serenity.PageHolder")
                out.appendLine("import net.derfruhling.serenity.PageDetails")
                out.appendLine("import net.derfruhling.serenity.PageHolderFactory")
                out.appendLine("import net.derfruhling.serenity.annotations.HtmlComposable")
                out.appendLine("import kotlinx.serialization.Serializable")
                out.appendLine("import kotlinx.serialization.Transient")
                out.appendLine("import kotlinx.serialization.SerialName")

                out.appendLine()
                out.appendLine("@Serializable")
                out.appendLine("@SerialName(\"${hashFunctionName(function.qualifiedName!!.asString())}\")")

                val parameters by lazy { PageParameters(function, logger) }
                val isClass = function.parameters.isNotEmpty()
                if (isClass) {
                    val props = parameters.params.joinToString(",\n") {
                        "@SerialName(\"${it.serialName}\") " +
                            "actual val ${it.propertyName}: " +
                            printType(it.typeRef)
                    }.prependIndent("    ")

                    out.appendLine("actual class ${function.simpleName.asString()} private constructor(\n    @Transient private val __serenity_generated: Unit = Unit,\n$props\n) : PageHolder<${function.simpleName.asString()}> {")
                } else {
                    out.appendLine("actual data object ${function.simpleName.asString()} : PageHolder<${function.simpleName.asString()}> {")
                }

                if (!function.annotations.any { it.annotationType.resolve().declaration.qualifiedName!!.asString() == "androidx.compose.runtime.Composable" }) {
                    logger.error("Pages must be composable", function)
                }
                val hashFunctionName = hashFunctionName(function.qualifiedName!!.asString())
                val annotation = function.getAnnotationsByType(RegisterPage::class).single()

                out.appendLine(
                    """
                    actual override val id: String = "$hashFunctionName"
                    actual override val path: String = "${
                        if (isClass) parameters.pathExpression(
                            annotation.path
                        ) else annotation.path
                    }"
                    actual override val details: PageDetails = ${generatePageDetails(annotation)}
                """.trimIndent().prependIndent("    ")
                )

                out.appendLine(
                    """
                    @Composable
                    @HtmlComposable
                    actual override fun Main() {
                        val _call: ApplicationCall = currentCall
                """.trimIndent().prependIndent("    ")
                )

                out.appendLine("        SideEffect { _call.attributes[pageFunctionName] = \"$hashFunctionName\" }")

                if (isClass) {
                    val paramNames = function.parameters.mapNotNull { it.name?.getShortName() }
                        .joinToString { "_$it" }
                    val receiver = function.parameters.find { it.name == null }
                        ?.let { "_serenity_receiver." } ?: ""
                    out.appendLine(
                        """ 
                        key($paramNames) {
                            $receiver${function.qualifiedName!!.asString()}($paramNames)
                        }
                    """.trimIndent().prependIndent("        ")
                    )
                } else {
                    out.appendLine("        ${function.qualifiedName!!.asString()}()")
                }

                out.appendLine("    }")

                if (isClass) {
                    val parseParams = parameters.params.joinToString(",\n") {
                        buildString {
                            append("${it.serialName} = ctx.parameters[\"${it.serialName}\"]?.let { ${it.parseExpr} }")

                            if(!it.type.isMarkedNullable) {
                                append(" ?: error(\"No value provided for parameter '${it.serialName}'\")")
                            }
                        }
                    }.prependIndent("            ")

                    out.appendLine(
                        """
                        actual companion object Factory : PageHolderFactory<ApplicationCall, ${function.simpleName.getShortName()}> {
                            actual override val id: String = "$hashFunctionName"
                            actual override val path: String = "${annotation.path}"
                            actual override fun create(ctx: ApplicationCall): ${function.simpleName.getShortName()} {
                                return of(
                        $parseParams
                                )
                            }
                            
                            actual fun of(${
                                parameters.params.joinToString {
                                    "${it.serialName}: ${printType(it.typeRef)}"
                                }
                            }): ${function.simpleName.getShortName()} {
                                return ${function.simpleName.getShortName()}(__serenity_generated = Unit, ${
                                parameters.params.joinToString {
                                    "${it.propertyName} = ${it.serialName}"
                                }
                            })
                            }
                        }
                    """.trimIndent().prependIndent("    ")
                    )
                }

                out.appendLine('}')
            }
        }
    }

    class Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return PageProcessor(environment.codeGenerator, environment.logger, environment.options)
        }
    }
}