package net.derfruhling.serenity.processor.internal

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import net.derfruhling.serenity.processor.printType

class LocalizationPlatformStructureProcessor(env: SymbolProcessorEnvironment) : SymbolProcessor {
    val codeGenerator = env.codeGenerator
    val logger = env.logger

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation("net.derfruhling.serenity.localization.PlatformGenerateStructure")
            .filterIsInstance<KSFile>()
            .filter { it.validate(enableNewFeatures = true) }
            .forEach {
                @Suppress("UNCHECKED_CAST")
                val targets = (it.annotations.find { a ->
                    a.annotationType.resolve().declaration.qualifiedName!!.asString() == "net.derfruhling.serenity.localization.PlatformGenerateStructure"
                }!!.arguments.first().value!! as Collection<String>).toList()

                it.accept(FunctionVisitor(), targets)
            }

        return emptyList()
    }

    inner class FunctionVisitor : KSEmptyVisitor<List<String>, Unit>(enableNewFeatures = true) {
        override fun defaultHandler(
            node: KSNode,
            data: List<String>
        ) {}

        override fun visitFile(file: KSFile, data: List<String>) {
            codeGenerator.createNewFile(Dependencies(aggregating = true, file), file.packageName.asString(), "StructureGen").bufferedWriter().use { out ->
                out.appendLine("@file:Suppress(\"NOTHING_TO_INLINE\")")
                out.appendLine("package ${file.packageName.asString()}")
                out.appendLine()
                out.appendLine("import androidx.compose.runtime.Composable")
                out.appendLine("import net.derfruhling.serenity.elements.*")
                out.appendLine()

                val decls = file.declarations
                    .filterIsInstance<KSFunctionDeclaration>()
                    .filter { it.simpleName.asString() == "TextOf" }
                    .map { it.parameters.drop(1) }

                for(key in data) {
                    for(decl in decls) {
                        out.appendLine("@Composable")
                        out.append("inline fun ${key}Of(value: ConstantName")

                        for(param in decl) {
                            out.append(", ")
                            if(param.isVararg) {
                                out.append("vararg ")
                            }

                            out.append("${param.name!!.asString()}: ${printType(param.type)}")
                        }

                        out.appendLine(") =")
                        out.append("    $key { TextOf(value")

                        for(param in decl) {
                            out.append(", ")
                            if(param.isVararg) {
                                out.append('*')
                            }

                            out.append(param.name!!.asString())
                        }

                        out.appendLine(") }")
                        out.appendLine()
                    }
                }
            }
        }
    }

    class Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return LocalizationPlatformStructureProcessor(environment)
        }
    }
}