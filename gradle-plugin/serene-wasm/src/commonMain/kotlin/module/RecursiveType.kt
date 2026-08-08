package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.Decode
import net.derfruhling.serene.wasm.DeferredDecode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter
import net.derfruhling.serene.wasm.map

sealed interface RecursiveType : Type {
    sealed interface SubType : RecursiveType {
        companion object : Decode<SubType> {
            override fun deferredDecode(reader: WasmReader): DeferredDecode<SubType>? {
                Final.nestedDecode(reader)?.let { return it }
                NonFinal.nestedDecode(reader)?.let { return it }
                return null
            }
        }
    }

    data class Compound(val subTypes: List<SubType>) : RecursiveType {
        override fun encode(out: WasmWriter) {
            out.writeByte(Constants.REC_TYPE_COMPOUND)
            out.writeList(subTypes)
        }

        companion object : Decode<Compound> {
            override fun deferredDecode(reader: WasmReader): DeferredDecode<Compound>? {
                if(reader.readByte() != Constants.REC_TYPE_COMPOUND) return null
                return DeferredDecode { Compound(it.readList { r -> SubType.decode(r) }) }
            }
        }
    }

    data class Final(val typeUses: List<UInt>, val compositeType: CompositeType) : SubType {
        override fun encode(out: WasmWriter) {
            if(typeUses.isNotEmpty()) {
                out.writeByte(Constants.SUB_TYPE_FINAL)
                out.writeList(typeUses, WasmWriter::writeUInt)
            }

            compositeType.encode(out)
        }

        companion object : Decode<Final> {
            override fun deferredDecode(reader: WasmReader): DeferredDecode<Final>? {
                CompositeType.nestedDecode(reader)?.let { return it.map { t -> Final(emptyList(), t) } }
                if(reader.readByte() != Constants.SUB_TYPE_FINAL) return null
                return DeferredDecode { Final(it.readList(WasmReader::readUInt), CompositeType.decode(it)) }
            }
        }
    }

    data class NonFinal(val typeUses: List<UInt>, val compositeType: CompositeType) : SubType {
        override fun encode(out: WasmWriter) {
            out.writeByte(Constants.SUB_TYPE_NON_FINAL)
            out.writeList(typeUses, WasmWriter::writeUInt)
            compositeType.encode(out)
        }

        companion object : Decode<NonFinal> {
            override fun deferredDecode(reader: WasmReader): DeferredDecode<NonFinal>? {
                if(reader.readByte() != Constants.SUB_TYPE_NON_FINAL) return null
                return DeferredDecode { NonFinal(it.readList(WasmReader::readUInt), CompositeType.decode(it)) }
            }
        }
    }

    companion object : Decode<RecursiveType> {
        override fun deferredDecode(reader: WasmReader): DeferredDecode<RecursiveType>? {
            SubType.nestedDecode(reader)?.let { return it }
            Compound.nestedDecode(reader)?.let { return it }
            return null
        }
    }
}