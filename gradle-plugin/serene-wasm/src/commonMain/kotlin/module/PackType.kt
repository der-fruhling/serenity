package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.Decode
import net.derfruhling.serene.wasm.DeferredDecode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.module.Type.SimpleType

sealed interface PackType : StorageType, Unpackable {
    override val valueType: ValueType
        get() = NumericType.I32

    data object I8 : SimpleType<I8>(Constants.PACK_TYPE_I8), PackType {
        override val size: Int
            get() = 1
    }

    data object I16 : SimpleType<I16>(Constants.PACK_TYPE_I16), PackType {
        override val size: Int
            get() = 2
    }

    companion object : Decode<PackType> {
        override fun deferredDecode(reader: WasmReader): DeferredDecode<PackType>? {
            return when(reader.readByte()) {
                Constants.PACK_TYPE_I8 -> I8
                Constants.PACK_TYPE_I16 -> I16
                else -> null
            }
        }
    }
}
