package net.derfruhling.serene.wasm.instruction

import kotlinx.io.bytestring.ByteString
import kotlinx.io.readByteString
import net.derfruhling.serene.wasm.Encode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter

data class VectorValue(val bytes: ByteString) : Encode {
    init {
        require(bytes.size == 16) { "Incorrect size for vector value" }
    }

    override fun encode(out: WasmWriter) {
        out.writeBytes(bytes)
    }

    constructor(reader: WasmReader) : this(reader.readBytes(16).readByteString())

    operator fun get(index: Int) = bytes[index]
}
