package net.derfruhling.serene.wasm

import kotlinx.io.Buffer
import net.derfruhling.serene.wasm.sections.Section

class WasmWriterModuleVisitor(val writer: WasmWriter) : ModuleVisitor {
    override fun visitMagic(magic: UInt, version: UInt) {
        writer.writeMagicUInt(magic)
        writer.writeStaticUInt(version)
    }

    override fun visit(section: Section) {
        writer.writeByte(section.id)
        val buffer = Buffer()
        val newWriter = WasmWriter(buffer)
        section.encode(newWriter)
        writer.writeUInt(buffer.size.toUInt())
        writer.writeBytes(buffer)
    }

    override fun visitEnd() {}
}