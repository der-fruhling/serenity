package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.Encode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter

data class Local(val count: UInt, val type: ValueType) : Encode {
    override fun encode(out: WasmWriter) {
        out.writeUInt(count)
        type.encode(out)
    }

    constructor(reader: WasmReader) : this(reader.readUInt(), ValueType.decode(reader))
}
