package net.derfruhling.html.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate

class ExpectProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
    options: Map<String, String>
) : SymbolProcessor {
    val packageName = options["net.derfruhling.compose-html.package"] ?: ""

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation("net.derfruhling.html.elements.Page")
            .filter { it.validate() }
            .filterIsInstance<KSFunctionDeclaration>()
            .toList()
            .forEach { it.accept(Acceptor(), Unit) }

        return emptyList()
    }

    inner class Acceptor : KSVisitorVoid() {
        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            codeGenerator.createNewFile(
                Dependencies(false, function.containingFile!!),
                packageName,
                function.simpleName.asString() + ".expect"
            ).bufferedWriter().use { out ->
                if (function.packageName.asString().isNotEmpty()) out.append("package ${function.packageName.asString()}\n\n")
                out.appendLine("import androidx.compose.runtime.Composable")
                out.appendLine("import net.derfruhling.html.PageHolder")
                out.appendLine("import net.derfruhling.html.annotations.HtmlComposable")
                out.appendLine("import kotlinx.serialization.Serializable")
                out.appendLine("import kotlinx.serialization.SerialName")

                out.appendLine()
                out.appendLine("""
                    @Serializable
                    @SerialName("${hashFunctionName(function.qualifiedName!!.asString())}")
                    expect object ${function.simpleName.asString()} : PageHolder {
                        override val id: String
                        override val path: String

                        @Composable
                        @HtmlComposable
                        override fun Main()
                    }
                """.trimIndent())
            }
        }
    }

    class Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return ExpectProcessor(environment.codeGenerator, environment.logger, environment.options)
        }
    }
}