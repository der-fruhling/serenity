package net.derfruhling.serene.wasm.sections

import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter

data class SourceMappingURLSection(val url: String) : CustomSection("sourceMappingURL") {
    override fun encodeCustom(out: WasmWriter) {
        out.writeString(url)
    }

    companion object : Factory<SourceMappingURLSection> {
        override fun parseFrom(reader: WasmReader): SourceMappingURLSection {
            val url = reader.readString()
            return SourceMappingURLSection(url)
        }
    }
}