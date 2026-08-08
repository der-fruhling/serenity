package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.DeferredDecode
import net.derfruhling.serene.wasm.Encode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter

sealed interface Type : Encode {
    sealed class SimpleType<T: SimpleType<T>>(val id: Byte) : Type, DeferredDecode<T> {
        override fun encode(out: WasmWriter) {
            out.writeByte(id)
        }

        override fun finishDecoding(reader: WasmReader): T {
            @Suppress("UNCHECKED_CAST")
            return this as T
        }
    }
}

