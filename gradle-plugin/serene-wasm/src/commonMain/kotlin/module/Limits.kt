package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.Decode
import net.derfruhling.serene.wasm.DeferredDecode
import net.derfruhling.serene.wasm.Encode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter
import net.derfruhling.serene.wasm.hasBit
import kotlin.experimental.or

data class Limits(val min: ULong, val max: ULong? = null, val wasm64: Boolean = false) : Encode {
    override fun encode(out: WasmWriter) {
        var byte = 0.toByte()
        if(max != null) byte = byte or 1
        if(wasm64) byte = byte or 4
        out.writeByte(byte)

        out.writeULong(min)
        max?.let { out.writeULong(it) }
    }

    companion object : Decode<Limits> {
        override fun deferredDecode(reader: WasmReader): DeferredDecode<Limits>? {
            val byte = reader.readByte()
            val hasMax = byte.hasBit(0)
            val wasm64 = byte.hasBit(2)
            return when(hasMax) {
                true -> DeferredDecode { Limits(it.readULong(), it.readULong(), wasm64) }
                false -> DeferredDecode { Limits(it.readULong(), wasm64 = wasm64) }
            }
        }
    }
}
