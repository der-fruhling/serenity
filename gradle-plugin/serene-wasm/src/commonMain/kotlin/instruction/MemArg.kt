package net.derfruhling.serene.wasm.instruction

import net.derfruhling.serene.wasm.Encode
import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.WasmWriter
import net.derfruhling.serene.wasm.hasBit
import kotlin.experimental.and
import kotlin.experimental.or

data class MemArg(val alignment: Byte, val offset: ULong, val memoryIndex: UInt) : Encode {
    init {
        require(alignment < 64) { "Alignment cannot be greater than 64" }
    }

    override fun encode(out: WasmWriter) {
        if(memoryIndex != 0u) {
            out.writeByte(alignment or MEMORY_INDEX_BIT)
            out.writeUInt(memoryIndex)
        } else {
            out.writeByte(alignment)
        }

        out.writeULong(offset)
    }

    companion object {
        private const val MEMORY_INDEX_BIT: Byte = 0x40
        private const val ALIGNMENT_MASK: Byte = 0x3f

        fun decode(reader: WasmReader): MemArg {
            val byte = reader.readByte()
            val memoryIndex = if(byte.hasBit(6)) {
                reader.readUInt()
            } else 0u
            val offset = reader.readULong()

            return MemArg(byte and ALIGNMENT_MASK, offset, memoryIndex)
        }
    }
}
