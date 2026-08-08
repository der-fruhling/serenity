package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.Decode
import net.derfruhling.serene.wasm.DeferredDecode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.module.Type.SimpleType

sealed interface VectorType : ValueType, Default, Unpackable {
    data object V128 : SimpleType<V128>(Constants.TYPE_V128), VectorType {
        override val size: Int
            get() = 16
    }

    companion object : Decode<VectorType> {
        override fun deferredDecode(reader: WasmReader): DeferredDecode<VectorType>? {
            return when(reader.readByte()) {
                Constants.TYPE_V128 -> V128
                else -> null
            }
        }
    }
}
