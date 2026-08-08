package net.derfruhling.serene.wasm.module

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.Encode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter

data class FieldType(val type: StorageType, val isMutable: Boolean) : Encode {
    override fun encode(out: WasmWriter) {
        type.encode(out)
        out.writeByte(when(isMutable) {
            false -> Constants.CONST
            true -> Constants.MUT
        })
    }

    constructor(reader: WasmReader) : this(
        type = StorageType.decode(reader),
        isMutable = when(val i = reader.readByte()) {
            Constants.CONST -> false
            Constants.MUT -> true
            else -> throw InvalidModuleDataException("Invalid mutability marker $i")
        }
    )
}
