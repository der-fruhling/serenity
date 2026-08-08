package net.derfruhling.serene.wasm.sections

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter

data class DataCountSection(val dataCount: UInt) : Section {
    override val id: Byte
        get() = Constants.DATA_COUNT_SECTION

    override fun encode(out: WasmWriter) {
        out.writeUInt(dataCount)
    }

    constructor(reader: WasmReader) : this(reader.readUInt())
}
