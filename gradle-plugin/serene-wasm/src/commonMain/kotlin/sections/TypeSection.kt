package net.derfruhling.serene.wasm.sections

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter
import net.derfruhling.serene.wasm.module.RecursiveType

data class TypeSection(val types: List<RecursiveType>) : Section {
    override val id: Byte
        get() = Constants.TYPE_SECTION

    override fun encode(out: WasmWriter) {
        out.writeList(types)
    }

    constructor(reader: WasmReader) : this(reader.readList { RecursiveType.decode(it) })

    companion object {
        val EMPTY = TypeSection(emptyList())
    }
}
