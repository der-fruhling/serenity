package net.derfruhling.serene.wasm.sections

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter
import net.derfruhling.serene.wasm.module.MemoryType

data class MemorySection(val memoryTypes: List<MemoryType>) : Section {
    override val id: Byte
        get() = Constants.MEMORY_SECTION

    override fun encode(out: WasmWriter) {
        out.writeList(memoryTypes)
    }

    constructor(reader: WasmReader) : this(reader.readList { MemoryType.decode(it) })

    companion object {
        val EMPTY = MemorySection(emptyList())
    }
}