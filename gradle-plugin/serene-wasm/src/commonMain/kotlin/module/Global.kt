package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.CodeBlob
import net.derfruhling.serene.wasm.Encode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter

data class Global(val type: GlobalType, val expr: CodeBlob) : Encode {
    override fun encode(out: WasmWriter) {
        type.encode(out)
        out.writeBytes(expr.byteString)
    }

    constructor(reader: WasmReader) : this(GlobalType(reader), reader.readExpr())
}