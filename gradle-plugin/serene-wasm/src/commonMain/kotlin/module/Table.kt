package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.CodeBlob
import net.derfruhling.serene.wasm.Decode
import net.derfruhling.serene.wasm.DeferredDecode
import net.derfruhling.serene.wasm.Encode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter

sealed interface Table : Encode {
    val type: TableType

    data class TableNull(override val type: TableType) : Table {
        override fun encode(out: WasmWriter) {
            type.encode(out)
        }
    }

    data class TableExpr(override val type: TableType, val expr: CodeBlob) : Table {
        override fun encode(out: WasmWriter) {
            out.writeByte(0x40)
            out.writeByte(0x00)
            type.encode(out)
            out.writeBytes(expr.byteString)
        }

        companion object : Decode<TableExpr> {
            override fun deferredDecode(reader: WasmReader): DeferredDecode<TableExpr>? {
                if(reader.readByte() != 0x40.toByte()) return null
                if(reader.readByte() != 0x00.toByte()) return null
                return DeferredDecode { TableExpr(TableType(it), it.readExpr()) }
            }
        }
    }

    companion object : Decode<Table> {
        override fun deferredDecode(reader: WasmReader): DeferredDecode<Table> {
            TableExpr.nestedDecode(reader)?.let { return it }
            return DeferredDecode { TableNull(TableType(reader)) }
        }
    }
}
