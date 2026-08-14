package net.derfruhling.serenity.build

import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.NormalizeLineEndings
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.BufferedWriter
import java.util.*
import javax.xml.parsers.SAXParserFactory
import kotlin.text.appendLine

abstract class GenerateStyles : DefaultTask() {
    @get:InputFile
    @get:NormalizeLineEndings
    abstract val sourceRules: RegularFileProperty

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    @get:ServiceReference("fetchData")
    abstract val fetchData: Property<FetchableDataService>

    init {
        outputs.doNotCacheIf("testing") { true }
        outputs.upToDateWhen { false }

        inputs.dir(fetchData.map { it.browserCompatData })
    }

    @TaskAction
    fun generate() {
        val outputDir = output.get()
        if (outputDir.asFile.exists()) {
            outputDir.asFile.listFiles()?.forEach { it.deleteRecursively() }
                ?: outputDir.asFile.also {
                    it.delete()
                    it.mkdir()
                }
        }

        val xml = SAXParserFactory.newInstance().newSAXParser()!!
        Handler(outputDir).use { xml.parse(sourceRules.get().asFile, it) }
    }

    class Handler(val output: Directory) : DefaultHandler(), AutoCloseable {
        private enum class State {
            UNPARSABLE,
            CANONICALLY_EMPTY,

            ROOT {
                override fun processingInstruction(h: Handler, target: String, data: String) {
                    when (target) {
                        "import" -> h.imports.add(data)
                    }
                }

                override fun startElement(
                    h: Handler,
                    name: String,
                    attributes: Attributes
                ): State {
                    return when (name) {
                        "rules" -> RULES
                        else -> UNPARSABLE
                    }
                }
            },

            RULES {
                override fun startElement(
                    h: Handler,
                    name: String,
                    attributes: Attributes
                ): State {
                    return when (name) {
                        "type" -> {
                            val id = attributes.getValue("id")!!
                            val kt = attributes.getValue("kt")?.split(' ')?.toTypedArray()
                            val union = attributes.getValue("union")?.split(' ')?.toTypedArray()
                            h.defineType(name = id, kotlin = kt, union)
                            CANONICALLY_EMPTY
                        }

                        "group" -> {
                            val name = attributes.getValue("name")!!
                            h.startGroup(name)
                            GROUP
                        }

                        else -> UNPARSABLE
                    }
                }

                override fun endElement(h: Handler, name: String) {
                    when (name) {
                        "group" -> h.endGroup()
                    }
                }
            },

            GROUP {
                override fun processingInstruction(h: Handler, target: String, data: String) {
                    when(target) {
                        "import" -> h.defineImport(data)
                    }
                }

                override fun startElement(
                    h: Handler,
                    name: String,
                    attributes: Attributes
                ): State {
                    val name = attributes.getValue("name")!!
                    val type = attributes.getValue("type")!!
                    h.defineRule(name, type)
                    return CANONICALLY_EMPTY
                }
            };

            open fun startElement(h: Handler, name: String, attributes: Attributes): State {
                return UNPARSABLE
            }

            open fun endElement(h: Handler, name: String) {}
            open fun characters(h: Handler, text: CharArray) {}
            open fun processingInstruction(h: Handler, target: String, data: String) {}
        }

        private class TypeInfo(
            val name: String,
            val kotlin: Array<String>?,
            val union: Array<String>?
        ) {
            val kotlinType by lazy {
                kotlin?.map {
                    if(it.startsWith('$'))
                        it.replaceFirst("$", "net.derfruhling.serenity.style.")
                    else it
                }?.toTypedArray()
            }

            override fun toString(): String = buildString {
                append("$name (${kotlin?.contentToString() ?: "union"})")

                union?.let { union ->
                    append("; of ${union.contentToString()}")
                }
            }
        }

        companion object {
            private val camelize = Regex("""-(\p{Ll})""")
        }

        private var file: BufferedWriter? = null
        private val imports = mutableListOf<String>()
        private val stack = Stack<State>()
        private var state = State.ROOT
        private val types = mutableMapOf<String, TypeInfo>()
        private val currentImports = mutableSetOf<String>()
        private lateinit var composable: String
        private var needsImportNewline = true

        private fun shortName(type: String): String {
            return if(type in currentImports) {
                type.substringAfterLast('.')
            } else type
        }

        private fun TypeInfo.eachType(fn: (String) -> Unit) {
            if(union != null) {
                for(u in union) {
                    (types[u] ?: throw InvalidUserDataException("Unknown type $u (in type $name union $union)")).eachType(fn)
                }
            } else {
                val types = kotlinType ?: throw InvalidUserDataException("Undefined type $name (missing kt or union attribute?)")

                for(type in types) {
                    fn(type)
                }
            }
        }

        fun defineType(name: String, kotlin: Array<String>?, union: Array<String>?) {
            types[name] = TypeInfo(name, kotlin, union)
        }

        fun startGroup(name: String) {
            if(file != null) throw IllegalStateException("Cannot start new group while another is active")

            currentImports.clear()
            output.file("Rules.$name.kt").asFile
                .bufferedWriter()
                .also {
                    file = it
                    it.appendLine("""
                        // This file was @generated from rules.xml.
                        // Group name: $name
                        // Do not edit this file manually. Your changes will be lost.
                    """.trimIndent())
                    it.initializeFile()
                }

            composable = shortName("androidx.compose.runtime.Composable")
            needsImportNewline = true
        }

        fun defineImport(import: String) = file!!.run {
            if(import.startsWith('@')) {
                types[import.substring(1)]!!.eachType {
                    if(currentImports.add(it)) {
                        appendLine("import $it")
                    }
                }
            } else if (currentImports.add(import)) {
                appendLine("import $import")
            }
        }

        fun defineRule(name: String, type: String) {
            val file = file
            if(file == null) throw IllegalStateException("Cannot start rule while no file is open")

            if (currentImports.isNotEmpty() && needsImportNewline) {
                file.newLine()
                needsImportNewline = false
            }

            val camelized = name.replace(camelize) { it.groupValues[1].uppercase() }
            val type = types[type] ?: throw IllegalStateException("Unknown type `$type` in rule `$name`")

            file.appendLine("//region rule $name: $type")

            var isFirst = true
            type.eachType {
                if(!isFirst) {
                    file.newLine()
                } else isFirst = false

                val short = shortName(it)

                // language=markdown
                val docs = buildString {
                    appendLine("[View MDN docs](https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/Properties/$name)")
                }.prependIndent(" * ")

                // language=kotlin
                file.appendLine("""
                    /**
                    $docs
                     */ 
                    @$composable
                    fun Style.$camelized(value: $short) =
                        Rule("$name", $short.notation, value)
                """.trimIndent())
            }

            file.appendLine("//endregion rule $name")
            file.newLine()
        }

        fun endGroup() {
            file?.close()
            file = null
        }

        // language=kotlin
        private fun BufferedWriter.initializeFile() {
            appendLine("package net.derfruhling.serenity.style")
            newLine()

            for (import in imports) {
                defineImport(import)
            }
        }

        override fun processingInstruction(target: String, data: String) {
            state.processingInstruction(this, target, data)
        }

        override fun startElement(
            uri: String,
            localName: String,
            qName: String,
            attributes: Attributes
        ) {
            val newState = state.startElement(this, qName, attributes)
            stack.push(state)
            state = newState
        }

        override fun endElement(
            uri: String,
            localName: String,
            qName: String
        ) {
            state = stack.pop()
            state.endElement(this, qName)
        }

        override fun close() {
            file?.close()
        }
    }
}