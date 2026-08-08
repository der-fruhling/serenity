package net.derfruhling.serene.wasm.sections

import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.Encode
import net.derfruhling.serene.wasm.WasmReader

sealed interface Section : Encode {
    val id: Byte

    companion object {
        fun decode(reader: WasmReader): Section {
            val byte = reader.readByte()
            val len = reader.readUInt()
            val keeper = reader.keeper.inherit()
            val bytes = reader.readBytes(len)
            val reader = WasmReader(bytes, keeper)

            return when(byte) {
                Constants.CUSTOM_SECTION -> UnidentifiedCustomSection(reader)
                Constants.TYPE_SECTION -> TypeSection(reader)
                Constants.IMPORT_SECTION -> ImportSection(reader)
                Constants.FUNCTION_SECTION -> FunctionSection(reader)
                Constants.TABLE_SECTION -> TableSection(reader)
                Constants.MEMORY_SECTION -> MemorySection(reader)
                Constants.GLOBAL_SECTION -> GlobalSection(reader)
                Constants.EXPORT_SECTION -> ExportSection(reader)
                Constants.START_SECTION -> StartSection(reader)
                Constants.ELEMENT_SECTION -> ElementSection(reader)
                Constants.CODE_SECTION -> CodeSection(reader)
                Constants.DATA_SECTION -> DataSection(reader)
                Constants.DATA_COUNT_SECTION -> DataCountSection(reader)
                Constants.TAG_SECTION -> TagSection(reader)
                else -> UnknownSection(byte, reader.consume())
            }
        }
    }
}
