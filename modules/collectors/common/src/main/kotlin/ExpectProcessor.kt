package net.derfruhling.serenity.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import net.derfruhling.serenity.annotations.RegisterPage

class ExpectProcessor(
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
        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            codeGenerator.createNewFile(
                Dependencies(false, function.containingFile!!),
                function.packageName.asString(),
                function.simpleName.asString() + ".expect"
            ).bufferedWriter().use { out ->
                if (function.packageName.asString().isNotEmpty()) out.append("package ${function.packageName.asString()}\n\n")
                out.appendLine("import androidx.compose.runtime.Composable")
                out.appendLine("import net.derfruhling.serenity.PageHolder")
                out.appendLine("import net.derfruhling.serenity.annotations.HtmlComposable")
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