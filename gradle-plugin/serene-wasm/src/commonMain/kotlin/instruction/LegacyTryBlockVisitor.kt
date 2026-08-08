package net.derfruhling.serene.wasm.instruction

interface LegacyTryBlockVisitor : InstructionVisitor {
    fun visitCatch(tagIndex: UInt): LegacyTryBlockVisitor
    fun visitCatchAll(): InstructionVisitor
    fun visitDelegate(label: UInt)
}
