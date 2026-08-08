package net.derfruhling.serene.wasm.sections

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter
import net.derfruhling.serene.wasm.module.Element

data class ElementSection(val elements: List<Element>) : Section {
    override val id: Byte
        get() = Constants.ELEMENT_SECTION

    override fun encode(out: WasmWriter) {
        out.writeList(elements)
    }

    constructor(reader: WasmReader) : this(reader.readList { Element.decode(it) })

    companion object {
        val EMPTY = ElementSection(emptyList())
    }
}
