package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.Decode
import net.derfruhling.serene.wasm.DeferredDecode
import net.derfruhling.serene.wasm.Encode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter

data class TagType(val typeIdx: UInt) : Encode {
    override fun encode(out: WasmWriter) {
        out.writeByte(0x00)
        out.writeUInt(typeIdx)
    }

    companion object : Decode<TagType> {
        override fun deferredDecode(reader: WasmReader): DeferredDecode<TagType> {
            val flags = reader.readByte()
            require(flags == 0.toByte()) { "Tag type flags must be zero" }
            return DeferredDecode { TagType(it.readUInt()) }
        }
    }
}
