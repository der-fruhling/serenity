package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.Decode
import net.derfruhling.serene.wasm.DeferredDecode
import net.derfruhling.serene.wasm.Encode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.map

data class MemoryType(val limits: Limits) : Encode by limits {
    companion object : Decode<MemoryType> {
        override fun deferredDecode(reader: WasmReader): DeferredDecode<MemoryType>? {
            return Limits.deferredDecode(reader)?.map(::MemoryType)
        }
    }
}