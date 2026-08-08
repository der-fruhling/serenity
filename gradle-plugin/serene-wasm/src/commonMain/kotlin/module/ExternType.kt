package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.Decode
import net.derfruhling.serene.wasm.DeferredDecode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter
import net.derfruhling.serene.wasm.map

sealed interface ExternType : Type {
    data class Func(val typeIdx: UInt) : ExternType {
        override fun encode(out: WasmWriter) {
            out.writeByte(0)
            out.writeUInt(typeIdx)
        }
    }

    data class Table(val tableType: TableType) : ExternType {
        override fun encode(out: WasmWriter) {
            out.writeByte(1)
            tableType.encode(out)
        }
    }

    data class Memory(val memType: MemoryType) : ExternType {
        override fun encode(out: WasmWriter) {
            out.writeByte(2)
            memType.encode(out)
        }
    }

    data class Global(val globalType: GlobalType) : ExternType {
        override fun encode(out: WasmWriter) {
            out.writeByte(3)
            globalType.encode(out)
        }
    }

    data class Tag(val tagType: TagType) : ExternType {
        override fun encode(out: WasmWriter) {
            out.writeByte(4)
            tagType.encode(out)
        }
    }

    companion object : Decode<ExternType> {
        override fun deferredDecode(reader: WasmReader): DeferredDecode<ExternType>? {
            return when(reader.readByte().toInt()) {
                0 -> DeferredDecode { Func(it.readUInt()) }
                1 -> DeferredDecode { Table(TableType(it)) }
                2 -> MemoryType.nestedDecode(reader)?.map(::Memory)
                3 -> DeferredDecode { Global(GlobalType(it)) }
                4 -> TagType.nestedDecode(reader)?.map(::Tag)
                else -> null
            }
        }
    }
}