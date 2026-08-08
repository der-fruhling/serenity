package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.CodeBlob
import net.derfruhling.serene.wasm.Encode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter

data class FunctionCode(val locals: List<Local>, val code: CodeBlob) : Encode {
    override fun encode(out: WasmWriter) {
        out.writeList(locals)
        out.writeBytes(code.byteString)
    }

    constructor(reader: WasmReader) : this(reader.readList(::Local), reader.readExpr()) {

    }
}