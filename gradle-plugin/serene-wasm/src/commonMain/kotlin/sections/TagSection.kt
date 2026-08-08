package net.derfruhling.serene.wasm.sections

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter
import net.derfruhling.serene.wasm.module.TagType

data class TagSection(val tags: List<TagType>) : Section {
    override val id: Byte
        get() = Constants.TAG_SECTION

    override fun encode(out: WasmWriter) {
        out.writeList(tags)
    }

    constructor(reader: WasmReader) : this(reader.readList { TagType.decode(it) })

    companion object {
        val EMPTY = TagSection(emptyList())
    }
}
