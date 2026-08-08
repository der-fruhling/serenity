package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.Encode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter
import kotlin.jvm.JvmInline

@JvmInline
value class ResultType(val types: List<ValueType>) : Encode {
    override fun encode(out: WasmWriter) {
        out.writeList(types)
    }

    constructor(reader: WasmReader) : this(reader.readList { ValueType.decode(it) })
}
