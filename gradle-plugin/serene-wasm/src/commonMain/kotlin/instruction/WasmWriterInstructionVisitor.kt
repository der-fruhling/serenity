package net.derfruhling.serene.wasm.instruction

import net.derfruhling.serene.wasm.WasmWriter
import net.derfruhling.serene.wasm.module.BlockType
import net.derfruhling.serene.wasm.module.HeapType
import net.derfruhling.serene.wasm.module.RefType
import net.derfruhling.serene.wasm.module.ValueType
import kotlin.experimental.or

private interface CommonWasmWriterInstructionVisitor : InstructionVisitor, LegacyExceptionsVisitor

class WasmWriterInstructionVisitor(val writer: WasmWriter) : CommonWasmWriterInstructionVisitor {
    override fun visitSimple(op: Op) {
        writer.writeOp(op)
    }

    override fun visitSelect(
        op: Op,
        types: List<ValueType>
    ) {
        writer.writeOp(op)
        writer.writeList(types)
    }

    override fun visitIndex(op: Op, index: UInt) {
        writer.writeOp(op)
        writer.writeUInt(index)
    }

    override fun visitIndices(
        op: Op,
        firstIndex: UInt,
        secondIndex: UInt
    ) {
        writer.writeOp(op)
        writer.writeUInt(firstIndex)
        writer.writeUInt(secondIndex)
    }

    override fun visitBranchTable(
        op: Op,
        branches: List<UInt>,
        fallback: UInt
    ) {
        writer.writeOp(op)
        writer.writeList(branches) { writeUInt(it) }
        writer.writeUInt(fallback)
    }

    override fun visitBranchOnCast(
        op: Op,
        labelIndex: UInt,
        from: RefType,
        to: RefType
    ) {
        writer.writeOp(op)
        var byte: Byte = 0
        if(from is RefType.Nullable) byte = byte or 1
        if(to is RefType.Nullable) byte = byte or 2
        writer.writeByte(byte)
        writer.writeUInt(labelIndex)
        from.heapType.encode(writer)
        to.heapType.encode(writer)
    }

    override fun visitBlock(
        op: Op,
        blockType: BlockType
    ): InstructionVisitor {
        writer.writeOp(op)
        blockType.encode(writer)
        return WasmWriterInstructionVisitor(writer)
    }

    override fun visitIf(
        op: Op,
        blockType: BlockType
    ): IfBlockVisitor {
        writer.writeOp(op)
        blockType.encode(writer)
        return IfElse()
    }

    override fun visitCatchTable(
        op: Op,
        blockType: BlockType,
        catches: List<Catch>
    ): InstructionVisitor {
        writer.writeOp(op)
        blockType.encode(writer)
        writer.writeList(catches)
        return WasmWriterInstructionVisitor(writer)
    }

    override fun visitMemoryOp(
        op: Op,
        memArg: MemArg
    ) {
        writer.writeOp(op)
        memArg.encode(writer)
    }

    override fun visitMemoryOpWithLane(
        op: Op,
        memArg: MemArg,
        laneIndex: Byte
    ) {
        writer.writeOp(op)
        memArg.encode(writer)
        writer.writeByte(laneIndex)
    }

    override fun visitWithHeapType(
        op: Op,
        type: HeapType
    ) {
        writer.writeOp(op)
        type.encode(writer)
    }

    override fun visitConstI32(op: Op, value: Int) {
        writer.writeOp(op)
        writer.writeInt(value)
    }

    override fun visitConstI64(op: Op, value: Long) {
        writer.writeOp(op)
        writer.writeLong(value)
    }

    override fun visitConstF32(
        op: Op,
        value: Float
    ) {
        writer.writeOp(op)
        writer.writeFloat(value)
    }

    override fun visitConstF64(
        op: Op,
        value: Double
    ) {
        writer.writeOp(op)
        writer.writeDouble(value)
    }

    override fun visitConstV128(
        op: Op,
        value: VectorValue
    ) {
        writer.writeOp(op)
        value.encode(writer)
    }

    override fun visitWithLane(
        op: Op,
        laneIndex: Byte
    ) {
        writer.writeOp(op)
        writer.writeByte(laneIndex)
    }

    override fun visitEnd() {
        writer.writeByte(0x0B)
    }

    override fun visitLegacyTry(blockType: BlockType): LegacyTryBlockVisitor {
        writer.writeByte(0x06)
        blockType.encode(writer)
        return LegacyTry()
    }

    private inner class IfElse : IfBlockVisitor, CommonWasmWriterInstructionVisitor by this {
        override fun visitElse(): InstructionVisitor {
            writer.writeByte(0x05)
            return WasmWriterInstructionVisitor(writer)
        }
    }

    private inner class LegacyTry : LegacyTryBlockVisitor, CommonWasmWriterInstructionVisitor by this {
        override fun visitCatch(tagIndex: UInt): LegacyTryBlockVisitor {
            writer.writeByte(0x07)
            writer.writeUInt(tagIndex)
            return LegacyTry()
        }

        override fun visitCatchAll(): InstructionVisitor {
            writer.writeByte(0x19)
            return WasmWriterInstructionVisitor(writer)
        }

        override fun visitDelegate(label: UInt) {
            writer.writeByte(0x18)
            writer.writeUInt(label)
        }
    }
}