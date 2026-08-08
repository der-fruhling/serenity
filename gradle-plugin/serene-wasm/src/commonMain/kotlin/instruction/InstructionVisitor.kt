package net.derfruhling.serene.wasm.instruction

import net.derfruhling.serene.wasm.module.BlockType
import net.derfruhling.serene.wasm.module.HeapType
import net.derfruhling.serene.wasm.module.RefType
import net.derfruhling.serene.wasm.module.ValueType

interface InstructionVisitor {
    fun visitSimple(op: Op)
    fun visitSelect(op: Op, types: List<ValueType>)
    fun visitIndex(op: Op, index: UInt)
    fun visitIndices(op: Op, firstIndex: UInt, secondIndex: UInt)
    fun visitBranchTable(op: Op, branches: List<UInt>, fallback: UInt)
    fun visitBranchOnCast(op: Op, labelIndex: UInt, from: RefType, to: RefType)
    fun visitBlock(op: Op, blockType: BlockType): InstructionVisitor
    fun visitIf(op: Op, blockType: BlockType): IfBlockVisitor
    fun visitCatchTable(op: Op, blockType: BlockType, catches: List<Catch>): InstructionVisitor
    fun visitMemoryOp(op: Op, memArg: MemArg)
    fun visitMemoryOpWithLane(op: Op, memArg: MemArg, laneIndex: Byte)
    fun visitWithHeapType(op: Op, type: HeapType)
    fun visitConstI32(op: Op, value: Int)
    fun visitConstI64(op: Op, value: Long)
    fun visitConstF32(op: Op, value: Float)
    fun visitConstF64(op: Op, value: Double)
    fun visitConstV128(op: Op, value: VectorValue)
    fun visitWithLane(op: Op, laneIndex: Byte)
    fun visitEnd()
}

