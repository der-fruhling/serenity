package net.derfruhling.serene.wasm.sections

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter
import net.derfruhling.serene.wasm.module.Global
import net.derfruhling.serene.wasm.module.GlobalType

data class GlobalSection(val globals: List<Global>) : Section {
    override val id: Byte
        get() = Constants.GLOBAL_SECTION

    override fun encode(out: WasmWriter) {
        out.writeList(globals)
    }

    constructor(reader: WasmReader) : this(reader.readList(::Global))

    companion object {
        val EMPTY = GlobalSection(emptyList())
    }
}
