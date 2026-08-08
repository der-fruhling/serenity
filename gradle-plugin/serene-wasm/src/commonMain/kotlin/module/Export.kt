package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.Encode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter

data class Export(val name: String, val kind: ExportType, val externIdx: UInt) : Encode {
    override fun encode(out: WasmWriter) {
        out.writeString(name)
        out.writeByte(kind.ordinal.toByte())
        out.writeUInt(externIdx)
    }

    constructor(reader: WasmReader) : this(
        reader.readString(),
        ExportType.entries[reader.readByte().toInt()],
        reader.readUInt()
    )
}
