package net.derfruhling.serenity.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import net.derfruhling.serenity.annotations.PlatformDefinitions
import net.derfruhling.serenity.annotations.RegisterPage

class ExpectProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
    options: Map<String, String>
) : SymbolProcessor {
    private var platformDefs: String? = null

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(PlatformDefinitions::class.qualifiedName!!)
            .filter { it.validate() }
            .filterIsInstance<KSFile>()
            .toList()
            .forEach { it.accept(PlatformAcceptor(), Unit) }

        resolver.getSymbolsWithAnnotation(RegisterPage::class.qualifiedName!!)
            .filter { it.validate() }
            .filterIsInstance<KSFunctionDeclaration>()
            .toList()
            .forEach { it.accept(Acceptor(), Unit) }

        return emptyList()
    }

    inner class PlatformAcceptor : KSVisitorVoid() {
        override fun visitFile(file: KSFile, data: Unit) {
            platformDefs = file.packageName.asString()
        }
    }

    inner class Acceptor : KSVisitorVoid() {
        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            codeGenerator.createNewFile(
                Dependencies(false, function.containingFile!!),
                function.packageName.asString(),
                function.simpleName.asString() + ".expect"
            ).bufferedWriter().use { out ->
                if (function.packageName.asString()
                        .isNotEmpty()
                ) out.append("package ${function.packageName.asString()}\n\n")
                out.appendLine("import androidx.compose.runtime.Composable")
                out.appendLine("import net.derfruhling.serenity.PageHolder")
                out.appendLine("import net.derfruhling.serenity.PageDetails")
                out.appendLine("import net.derfruhling.serenity.annotations.HtmlComposable")
                out.appendLine("import kotlinx.serialization.Serializable")
                out.appendLine("import kotlinx.serialization.SerialName")

                if (function.parameters.isEmpty()) {
                    out.appendLine()
                    out.appendLine(
                        """
                            @Serializable
                            @SerialName("${hashFunctionName(function.qualifiedName!!.asString())}")
                            expect object ${function.simpleName.asString()} : PageHolder<${function.simpleName.asString()}> {
                                override val id: String
                                override val path: String
                                override val details: PageDetails
        
                                @Composable
                                @HtmlComposable
                                override fun Main()
                            }
                        """.trimIndent()
                    )
                } else {
                    out.appendLine("import net.derfruhling.serenity.PageHolderFactory")

                    val defsPackage = platformDefs ?: run {
                        logger.error("Parameterized pages require a @PlatformDefinitions file to be present", function)
                        return
                    }

                    out.appendLine("import $defsPackage.PlatformContext")
                    out.appendLine()

                    val params = function.parameters.joinToString(",\n") {
                        "${it.name?.getShortName() ?: "_receiver"}: " +
                            printType(it.type)
                    }.prependIndent("            ")

                    val props = function.parameters.joinToString("\n") {
                        "@SerialName(\"${it.name?.getShortName() ?: $$"$receiver"}\") " +
                            "val _${it.name?.getShortName() ?: "serenity_receiver"}: " +
                            printType(it.type)
                    }.prependIndent("    ")

                    out.appendLine(
                        """
                            expect class ${function.simpleName.asString()} : PageHolder<${function.simpleName.asString()}> {
                                override val id: String
                                override val path: String
                                override val details: PageDetails
                                
                            $props
        
                                @Composable
                                @HtmlComposable
                                override fun Main()
                                
                                companion object Factory : PageHolderFactory<PlatformContext, ${function.simpleName.asString()}> {
                                    fun of(
                            $params
                                    ): ${function.simpleName.asString()}
                                    override val id: String
                                    override val path: String
                                    override fun create(ctx: PlatformContext): ${function.simpleName.asString()}
                                }
                            }
                        """.trimIndent()
                    )
                }
            }
        }
    }

    class Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return ExpectProcessor(
                environment.codeGenerator,
                environment.logger,
                environment.options
            )
        }
    }
}