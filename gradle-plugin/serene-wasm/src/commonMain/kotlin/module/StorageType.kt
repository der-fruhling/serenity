package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.Decode
import net.derfruhling.serene.wasm.DeferredDecode
import net.derfruhling.serene.wasm.WasmReader

sealed interface StorageType : Type {
    val valueType: ValueType

    companion object : Decode<StorageType> {
        override fun deferredDecode(reader: WasmReader): DeferredDecode<StorageType>? {
            ValueType.nestedDecode(reader)?.let { return it }
            PackType.nestedDecode(reader)?.let { return it }
            return null
        }
    }
}
