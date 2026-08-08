package net.derfruhling.serene.wasm.instruction

interface IfBlockVisitor : InstructionVisitor {
    fun visitElse(): InstructionVisitor
}

