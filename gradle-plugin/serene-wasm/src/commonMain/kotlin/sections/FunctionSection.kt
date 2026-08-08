package net.derfruhling.serene.wasm.sections

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter

data class FunctionSection(val types: List<UInt>) : Section {
    override val id: Byte
        get() = Constants.FUNCTION_SECTION

    override fun encode(out: WasmWriter) {
        out.writeList(types) { writeUInt(it) }
    }

    constructor(reader: WasmReader) : this(reader.readList { it.readUInt() })

    companion object {
        val EMPTY = FunctionSection(listOf())
    }
}
