package net.derfruhling.serenity.processor.internal

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.google.devtools.ksp.visitor.KSTopDownVisitor
import net.derfruhling.serenity.annotations.GenerateServerStubs
import net.derfruhling.serenity.processor.printModifiers
import net.derfruhling.serenity.processor.printType
import java.io.BufferedWriter

class PlatformDefinitionsProcessor(env: SymbolProcessorEnvironment) : SymbolProcessor {
    val codeGenerator = env.codeGenerator
    val logger = env.logger
    val platforms = env.platforms
    lateinit var any: KSType
    lateinit var unit: KSTypeReference

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if(platforms.size > 1) return emptyList()
        any = resolver.builtIns.anyType
        unit = resolver.createKSTypeReferenceFromKSType(resolver.builtIns.unitType)

        if(platforms.all { it is JvmPlatformInfo || it is NativePlatformInfo }) {
            resolver.getSymbolsWithAnnotation(GenerateServerStubs::class.qualifiedName!!)
                .filterIsInstance<KSFile>()
                .filter { it.validate() }
                .forEach {
                    codeGenerator.createNewFile(
                        Dependencies(false, it),
                        it.packageName.asString(),
                        it.fileName.replace(".kt", ".stub")
                    ).bufferedWriter().use { writer ->
                        writer.appendLine("@file:OptIn(UsedByGeneratedCode::class)")
                        writer.appendLine("package ${it.packageName.asString()}")
                        writer.appendLine()
                        writer.appendLine("import net.derfruhling.serenity.annotations.*")
                        writer.appendLine()
                        it.accept(FileVisitor(), writer)
                    }
                }
        }

        return emptyList()
    }

    inner class FileVisitor : KSTopDownVisitor<BufferedWriter, Unit>() {
        private var indent = ""
        private var inExpectContext = false
        private var inInterfaceContext = false

        private fun increaseIndent() {
            indent += "\t"
        }

        private fun decreaseIndent() {
            indent = indent.substring(0, indent.length - 1)
        }

        private fun BufferedWriter.indented(string: String) {
            appendLine(indent + string)
        }

        override fun defaultHandler(
            node: KSNode,
            data: BufferedWriter
        ) {}

        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: BufferedWriter
        ) {
            if(classDeclaration.isExpect) {
                data.indented(buildString {
                    val keyword = when(classDeclaration.classKind) {
                        ClassKind.INTERFACE -> "interface"
                        ClassKind.OBJECT -> "object"
                        else -> "class"
                    }
                    append("@Stub actual ${printModifiers(classDeclaration, noExpect = true)}$keyword ${classDeclaration.simpleName.asString()} ")

                    when(classDeclaration.classKind) {
                        ClassKind.ANNOTATION_CLASS, ClassKind.ENUM_CLASS, ClassKind.CLASS ->
                            append("@Stub internal constructor() ")
                        else -> {}
                    }

                    val superTypes = classDeclaration.superTypes.toList()
                    if(superTypes.isNotEmpty()) {
                        var isFirst = true
                        for(typeRef in superTypes) {
                            val type = typeRef.resolve()
                            if(type == any) continue

                            if(!isFirst) {
                                append(", ")
                            } else {
                                append(": ")
                                isFirst = false
                            }

                            append(printType(typeRef))

                            val typeDecl = type.declaration
                            if(typeDecl is KSClassDeclaration && typeDecl.classKind != ClassKind.INTERFACE) {
                                val constructors = typeDecl.getConstructors().toList()
                                if(constructors.any { it.parameters.isEmpty() } || constructors.isEmpty()) {
                                    append("()")
                                } else {
                                    logger.error("No empty constructor for super class", classDeclaration)
                                }
                            }
                        }
                    }

                    append(" {")
                })
                increaseIndent()
                inExpectContext = true
                inInterfaceContext = classDeclaration.classKind == ClassKind.INTERFACE
                super.visitClassDeclaration(classDeclaration, data)
                inExpectContext = false
                inInterfaceContext = false
                decreaseIndent()
                data.indented("}")
            } else {
                super.visitClassDeclaration(classDeclaration, data)
            }
        }

        override fun visitPropertyDeclaration(
            property: KSPropertyDeclaration,
            data: BufferedWriter
        ) {
            if(inExpectContext || property.isExpect) {
                data.indented(buildString {
                    append("@Stub actual ${printModifiers(property, noExpect = true)}")
                    append(if(property.isMutable) "var " else "val ")
                    property.extensionReceiver?.let {
                        append("(${printType(it)}).")
                    }
                    append(property.simpleName.asString())
                    append(": ${printType(property.type)}")

                    if(!inInterfaceContext) {
                        append(" by notImplemented")
                    }
                })
            }
        }

        override fun visitFunctionDeclaration(
            function: KSFunctionDeclaration,
            data: BufferedWriter
        ) {
            if(inExpectContext || function.isExpect) {
                data.indented(buildString {
                    append("@Stub actual ${printModifiers(function, noExpect = true)}fun ")
                    function.extensionReceiver?.let {
                        append("(${printType(it)}).")
                    }
                    append(function.simpleName.asString())
                    append("(")

                    var isFirstParam: Boolean = true
                    for(param in function.parameters) {
                        if(!isFirstParam) {
                            append(", ")
                        } else isFirstParam = false
                        if(param.isCrossInline) append("crossinline ")
                        if(param.isNoInline) append("noinline ")
                        if(param.isVararg) append("vararg ")
                        append(param.name?.asString() ?: logger.warn("Unnamed parameter", param))
                        append(": ${printType(param.type)}")
                    }

                    append("): ${printType(function.returnType ?: unit)}")

                    if(!inInterfaceContext) {
                        append(" = throw NotImplementedError()")
                    }
                })
            }
        }
    }

    class Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return PlatformDefinitionsProcessor(environment)
        }
    }
}