package net.derfruhling.serene.wasm.module

import kotlinx.io.bytestring.ByteString
import kotlinx.io.readByteString
import net.derfruhling.serene.wasm.*

class DataSegment private constructor(
    val memoryIndex: UInt,
    val offsetExpr: CodeBlob?,
    val bytes: ByteString
) : Encode {
    override fun encode(out: WasmWriter) {
        out.writeByte(
            if (offsetExpr == null) {
                1
            } else {
                if (memoryIndex == 0u) {
                    0
                } else {
                    2
                }
            }
        )

        if (memoryIndex != 0u) out.writeUInt(memoryIndex)
        if (offsetExpr != null) out.writeBytes(offsetExpr.byteString)
        out.writeUInt(bytes.size.toUInt())
        out.writeBytes(bytes)
    }

    companion object : Decode<DataSegment> {
        private fun WasmReader.readByteList(): ByteString {
            val size = readUInt()
            return readBytes(size).readByteString()
        }

        override fun deferredDecode(reader: WasmReader): DeferredDecode<DataSegment>? {
            return when (reader.readByte().toInt()) {
                0 -> DeferredDecode {
                    active(it.readExpr(), it.readByteList())
                }

                1 -> DeferredDecode {
                    passive(it.readByteList())
                }

                2 -> DeferredDecode {
                    active(
                        it.readUInt(),
                        it.readExpr(),
                        it.readByteList()
                    )
                }

                else -> null
            }
        }

        fun passive(bytes: ByteString) =
            DataSegment(0u, null, bytes)

        fun active(offsetExpr: CodeBlob, bytes: ByteString) =
            DataSegment(0u, offsetExpr, bytes)

        fun active(memoryIndex: UInt, offsetExpr: CodeBlob, bytes: ByteString) =
            DataSegment(memoryIndex, offsetExpr, bytes)
    }
}