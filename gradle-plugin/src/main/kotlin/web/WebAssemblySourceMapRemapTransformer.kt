package net.derfruhling.serenity.gradle.web

import net.derfruhling.serene.wasm.AbstractModuleVisitor
import net.derfruhling.serene.wasm.ModuleVisitor
import net.derfruhling.serene.wasm.WasmModule
import net.derfruhling.serene.wasm.sections.SourceMappingURLSection
import net.derfruhling.serene.wasm.sections.UnidentifiedCustomSection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Internal
import org.jetbrains.kotlin.konan.file.File

interface WebAssemblySourceMapRemapTransformer : SourceAwareWebAssemblyTransformer {
    @get:Internal
    val oldSourceMapDir: DirectoryProperty

    @get:Internal
    val sourceDirectory: DirectoryProperty
}

internal abstract class WebAssemblySourceMapRemapTransformerImpl :
    WebAssemblySourceMapRemapTransformer {
    override fun transformSourceMap(
        module: WasmModule,
        sourceMap: SourceMap
    ): SourceMap {
        val file = newBinary.get().asFile.toRelativeString(newSourceMap.get().asFile.parentFile)
        val sourceDir = oldSourceMapDir.get()
        val sources = sourceMap.sources.toMutableList()
        val sourcesContent = (sourceMap.sourcesContent ?: mutableListOf()).toMutableList()

        for ((i, sourcePath) in sourceMap.sources.withIndex()) {
            val sourceFile = sourceDir.file(sourcePath.replace('/', File.separatorChar)).asFile

            if (sourceFile.exists()) {
                sources[i] = sourceFile.toRelativeString(sourceDirectory.get().asFile)
                sourcesContent[i] = sourceFile.readText()
            }
        }

        return sourceMap.copy(
            file = file,
            sources = sources,
            sourcesContent = sourcesContent.takeIf { it.any { v -> v != null } }
        )
    }

    override fun transformBinary(binary: WasmModule, visitor: ModuleVisitor): ModuleVisitor {
        return Visitor(visitor)
    }

    private inner class Visitor(base: ModuleVisitor) : AbstractModuleVisitor(base) {
        var sourceMappingURLEmitted = false

        override fun visitCustomSection(section: UnidentifiedCustomSection) {
            if (section.name == "sourceMappingURL") {
                val sourceMapping = section.parse(SourceMappingURLSection)
                visitCustomSection(sourceMapping.copy(url = makeURL()))
                sourceMappingURLEmitted = true
            } else {
                super.visitCustomSection(section)
            }
        }

        private fun makeURL(): String =
            newSourceMap.get().asFile.toRelativeString(newBinary.get().asFile.parentFile)

        override fun visitEnd() {
            if (!sourceMappingURLEmitted) {
                visitCustomSection(SourceMappingURLSection(url = makeURL()))
            }
            super.visitEnd()
        }
    }
}