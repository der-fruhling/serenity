package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.*

sealed interface BlockType : Encode {
    data object Void : BlockType, Decode<Void> {
        override fun encode(out: WasmWriter) {
            out.writeByte(0x40)
        }

        override fun deferredDecode(reader: WasmReader): DeferredDecode<Void>? {
            return if(reader.readByte() == 0x40.toByte()) DeferredDecode { Void }
            else null
        }
    }

    data class ByIndex(val typeIndex: UInt) : BlockType {
        override fun encode(out: WasmWriter) {
            out.writeUInt(typeIndex)
        }
    }

    companion object : Decode<BlockType> {
        override fun deferredDecode(reader: WasmReader): DeferredDecode<BlockType>? {
            Void.nestedDecode(reader)?.let { return it }
            ValueType.nestedDecode(reader)?.let { return it }
            val v = reader.readLong()
            return if(v < 0) null
            else DeferredDecode { ByIndex(v.toUInt()) }
        }
    }
}