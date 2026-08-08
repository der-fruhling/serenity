package net.derfruhling.serenity.gradle.web

import kotlinx.io.Source
import net.derfruhling.serene.wasm.WasmModule
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

abstract class WasmParserService : BuildService<BuildServiceParameters.None> {
    fun parseBinary(source: Source): WasmModule {
        return WasmModule.parse(source)
    }
}
