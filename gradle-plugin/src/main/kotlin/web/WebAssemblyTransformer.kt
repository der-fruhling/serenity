package net.derfruhling.serenity.gradle.web

import net.derfruhling.serene.wasm.ModuleVisitor
import net.derfruhling.serene.wasm.WasmModule
import org.gradle.api.Named
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Internal
import java.io.Serializable

interface WebAssemblyTransformer : Serializable, Named {
    @Internal
    override fun getName(): String

    @get:Internal
    val newBinary: RegularFileProperty

    @get:Internal
    val newSourceMap: RegularFileProperty

    fun transformBinary(binary: WasmModule, visitor: ModuleVisitor): ModuleVisitor?
}
