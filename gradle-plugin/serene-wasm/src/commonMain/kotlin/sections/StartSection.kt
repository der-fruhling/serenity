package net.derfruhling.serene.wasm.sections

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter

data class StartSection(val startFunctionIdx: UInt) : Section {
    override val id: Byte
        get() = Constants.START_SECTION

    override fun encode(out: WasmWriter) {
        out.writeUInt(startFunctionIdx)
    }

    constructor(reader: WasmReader) : this(reader.readUInt())
}
