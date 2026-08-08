package net.derfruhling.serene.wasm.sections

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter
import net.derfruhling.serene.wasm.module.Import

data class ImportSection(val imports: List<Import>) : Section {
    override val id: Byte
        get() = Constants.IMPORT_SECTION

    override fun encode(out: WasmWriter) {
        out.writeList(imports)
    }

    constructor(reader: WasmReader) : this(reader.readList(::Import))

    companion object {
        val EMPTY = ImportSection(emptyList())
    }
}
