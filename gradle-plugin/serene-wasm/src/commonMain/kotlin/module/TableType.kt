package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.Encode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter

data class TableType(val refType: RefType, val limits: Limits) : Encode {
    override fun encode(out: WasmWriter) {
        refType.encode(out)
        limits.encode(out)
    }

    constructor(reader: WasmReader) : this(
        RefType.decode(reader),
        Limits.decode(reader)
    )
}