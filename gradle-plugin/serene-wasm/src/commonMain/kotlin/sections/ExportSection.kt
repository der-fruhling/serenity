package net.derfruhling.serene.wasm.sections

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter
import net.derfruhling.serene.wasm.module.Export

data class ExportSection(val exports: List<Export>) : Section {
    override val id: Byte
        get() = Constants.EXPORT_SECTION

    override fun encode(out: WasmWriter) {
        out.writeList(exports)
    }

    constructor(reader: WasmReader) : this(reader.readList(::Export))

    companion object {
        val EMPTY = ExportSection(emptyList())
    }
}
