package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.Decode
import net.derfruhling.serene.wasm.DeferredDecode
import net.derfruhling.serene.wasm.WasmReader

sealed interface ValueType : StorageType, BlockType {
    override val valueType: ValueType
        get() = this
    companion object : Decode<ValueType> {
        override fun deferredDecode(reader: WasmReader): DeferredDecode<ValueType>? {
            NumericType.nestedDecode(reader)?.let { return it }
            VectorType.nestedDecode(reader)?.let { return it }
            RefType.nestedDecode(reader)?.let { return it }
            return null
        }
    }
}
