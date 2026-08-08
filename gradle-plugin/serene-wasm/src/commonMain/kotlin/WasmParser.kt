package net.derfruhling.serene.wasm

import net.derfruhling.serene.wasm.sections.Section

open class WasmParser(reader: WasmReader, visitor: ModuleVisitor) : AbstractWasmParser(
    reader,
    visitor
) {
    override fun parseModule() {
        val magic = reader.readMagicUInt()
        val version = reader.readStaticUInt()
        visitor.visitMagic(magic, version)

        while(!reader.isExhausted) {
            val section = Section.decode(reader)
            visitor.visit(section)
        }

        visitor.visitEnd()
    }
}
