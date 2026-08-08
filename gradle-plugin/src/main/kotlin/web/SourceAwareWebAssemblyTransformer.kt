package net.derfruhling.serenity.gradle.web

import net.derfruhling.serene.wasm.WasmModule

interface SourceAwareWebAssemblyTransformer : WebAssemblyTransformer {
    fun transformSourceMap(module: WasmModule, sourceMap: SourceMap): SourceMap
}
