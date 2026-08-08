package net.derfruhling.serene.wasm.sections

import kotlinx.io.bytestring.ByteString
import net.derfruhling.serene.wasm.WasmWriter

data class UnknownSection(override val id: Byte, val bytes: ByteString) : Section {
    override fun encode(out: WasmWriter) {
        out.writeBytes(bytes)
    }
}
