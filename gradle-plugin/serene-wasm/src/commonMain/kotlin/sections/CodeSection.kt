package net.derfruhling.serene.wasm.sections

import kotlinx.io.Buffer
import net.derfruhling.serene.wasm.Constants
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter
import net.derfruhling.serene.wasm.module.FunctionCode
import net.derfruhling.serene.wasm.module.InvalidModuleDataException

data class CodeSection(val codes: List<FunctionCode>) : Section {
    override val id: Byte
        get() = Constants.CODE_SECTION

    override fun encode(out: WasmWriter) {
        out.writeList(codes) { code ->
            val buffer = Buffer()
            code.encode(WasmWriter(buffer))
            out.writeUInt(buffer.size.toUInt())
            out.writeBytes(buffer)
        }
    }

    companion object {
        val EMPTY = CodeSection(emptyList())

        private fun decodeList(reader: WasmReader): List<FunctionCode> {
            return reader.readList {
                val size = reader.readUInt()
                val keeper = reader.keeper.inherit()
                val buffer = reader.readBytes(size)
                val code = FunctionCode(WasmReader(buffer, keeper))
                if(!buffer.exhausted())
                    throw InvalidModuleDataException("Function code not exhausted by parsing")
                code
            }
        }
    }

    constructor(reader: WasmReader) : this(decodeList(reader))
}
