package net.derfruhling.serene.wasm

abstract class AbstractWasmParser(protected val reader: WasmReader, protected val visitor: ModuleVisitor) {
    abstract fun parseModule()
}
