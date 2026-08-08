package net.derfruhling.serene.wasm.sections

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter
import net.derfruhling.serene.wasm.module.DataSegment

data class DataSection(val data: List<DataSegment>) : Section {
    override val id: Byte
        get() = Constants.DATA_SECTION

    override fun encode(out: WasmWriter) {
        out.writeList(data)
    }

    constructor(reader: WasmReader) : this(reader.readList { DataSegment.decode(it) })

    companion object {
        val EMPTY = DataSection(emptyList())
    }
}
