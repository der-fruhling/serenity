package net.derfruhling.serene.wasm.sections

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter
import net.derfruhling.serene.wasm.module.Table

data class TableSection(val tables: List<Table>) : Section {
    override val id: Byte
        get() = Constants.TABLE_SECTION

    override fun encode(out: WasmWriter) {
        out.writeList(tables)
    }

    constructor(reader: WasmReader) : this(reader.readList { Table.decode(it) })

    companion object {
        val EMPTY = TableSection(emptyList())
    }
}
