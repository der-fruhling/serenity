package net.derfruhling.serene.wasm

import net.derfruhling.serene.wasm.sections.Section

interface ModuleVisitor {
    fun visitMagic(magic: UInt, version: UInt)
    fun visit(section: Section)
    fun visitEnd()

    fun visit(module: WasmModule) {
        visitMagic(Constants.MAGIC, Constants.VERSION)
        for(section in module.sections) {
            visit(section)
        }

        visitEnd()
    }
}
