package net.derfruhling.serenity.gradle.web

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import net.derfruhling.serene.wasm.MutableWasmModule
import net.derfruhling.serene.wasm.WasmWriter
import net.derfruhling.serene.wasm.WasmWriterModuleVisitor
import org.gradle.api.DefaultTask
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.*
import org.gradle.internal.logging.events.ProgressStartEvent
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import org.gradle.kotlin.dsl.registerBinding
import java.io.File
import javax.inject.Inject

abstract class TransformWebAssembly @Inject constructor(
    private val progressLoggerFactory: ProgressLoggerFactory,
    private val fileOperations: FileSystemOperations
) : DefaultTask() {
    @get:Nested
    abstract val transformers: ExtensiblePolymorphicDomainObjectContainer<WebAssemblyTransformer>

    init {
        transformers.registerBinding(
            WebAssemblySourceMapRemapTransformer::class,
            WebAssemblySourceMapRemapTransformerImpl::class
        )

        outputs.doNotCacheIf("testing") { true }
        outputs.upToDateWhen { false }
    }

    @get:InputDirectory
    @get:SkipWhenEmpty
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputBinaryDir: DirectoryProperty

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputSourceMap: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:ServiceReference("wasmParser")
    abstract val parserService: Property<WasmParserService>

    @get:Internal
    val canTransformSourceMaps
        get() = inputSourceMap.isPresent

    private val logger = Logging.getLogger(TransformWebAssembly::class.java)!!

    private fun sink(file: File) =
        SystemFileSystem.sink(Path(file.absolutePath)).buffered()

    private fun source(file: File) =
        SystemFileSystem.source(Path(file.absolutePath)).buffered()

    @TaskAction
    protected open fun transform() {
        if(outputDir.get().asFile.exists()) {
            for(file in outputDir.get().asFile.listFiles()!!) {
                file.deleteRecursively()
            }
        }

        val op = Class.forName(ProgressStartEvent.BUILD_OP_CATEGORY)
        val parent = progressLoggerFactory.newOperation(op)
        parent.start("Transform WebAssembly", "Reading...")
        try {
            actuallyTransform(parent, op)
            parent.completed()
        } catch (e: Exception) {
            parent.completed(e.toString(), true)
            throw GradleException("Error while transforming WebAssembly binary", e)
        }
    }

    private fun actuallyTransform(parent: ProgressLogger, opClass: Class<*>) {
        val inputBinary = inputBinaryDir.asFileTree.find { it.extension == "wasm" }!!
        var binary = source(inputBinary).use { parserService.get().parseBinary(it) }
        val canTransformSourceMaps = canTransformSourceMaps
        var sourceMap =
            inputSourceMap.takeIf { canTransformSourceMaps }?.get()?.asFile?.readText()?.let {
                Json.decodeFromString<SourceMap>(it)
            }

        val newBinaryFile = outputDir.file(inputBinary.name)
        val newSourceMap = outputDir.file(inputBinary.name + ".map")

        if (transformers.isNotEmpty()) {
            parent.progress("Transforming...")
            logger.info("Registered transformers to run: {}", transformers.size)
            var newBinary = MutableWasmModule()

            for (tf in transformers) {
                logger.info("Running transformer: {}", tf)
                val op = progressLoggerFactory.newOperation(opClass, parent)

                tf.newBinary.set(newBinaryFile)
                if(sourceMap != null) {
                    tf.newSourceMap.set(newSourceMap)
                }

                op.start(tf.javaClass.name, "Running transformer")
                try {
                    val visitor = tf.transformBinary(binary, newBinary) ?: run {
                        op.completed("Skipped", false)
                        continue
                    }

                    visitor.visit(binary)
                    op.progress("Finalizing")
                    binary = newBinary.finish()
                    newBinary = MutableWasmModule()

                    if (canTransformSourceMaps && tf is SourceAwareWebAssemblyTransformer) {
                        op.progress("Managing source map")
                        sourceMap = tf.transformSourceMap(binary, sourceMap!!)
                    }

                    op.completed()
                } catch (e: Exception) {
                    op.completed(e.message ?: "", true)
                    throw GradleException(
                        "Error while running transformer of type ${tf.javaClass.name}",
                        e
                    )
                }
            }
        }

        parent.progress("Writing output...")
        sink(newBinaryFile.get().asFile).use { out ->
            val writer = WasmWriterModuleVisitor(WasmWriter(out))
            writer.visit(binary)
        }

        sourceMap?.let { map ->
            newSourceMap.get().asFile.writeText(Json.encodeToString(map))
        }

        didWork = true
    }
}
