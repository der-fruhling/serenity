package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.Encode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter

data class Import(val module: String, val name: String, val externType: ExternType) : Encode {
    override fun encode(out: WasmWriter) {
        out.writeString(module)
        out.writeString(name)
        externType.encode(out)
    }

    constructor(reader: WasmReader) : this(
        reader.readString(),
        reader.readString(),
        ExternType.decode(reader)
    )
}
