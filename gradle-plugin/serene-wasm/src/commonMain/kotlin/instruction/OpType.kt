package net.derfruhling.serene.wasm.instruction

import net.derfruhling.serene.wasm.WasmReader
import net.derfruhling.serene.wasm.hasBit
import net.derfruhling.serene.wasm.module.*

enum class OpType {
    /**
     * Includes "parametric" opcodes.
     *
     * `inst`
     */
    SIMPLE {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor {
            visitor.visitSimple(op)
            return visitor
        }
    },

    /**
     * `inst` ~ list([ValueType])
     */
    SELECT_ARGS {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor {
            val types = reader.readList { ValueType.decode(it) }
            visitor.visitSelect(op, types)
            return visitor
        }
    },

    /**
     * `inst` ~ [UInt] (generic index)
     */
    SIMPLE_WITH_INDEX {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor {
            val index = reader.readUInt()
            visitor.visitIndex(op, index)
            return visitor
        }
    },

    /**
     * `inst` ~ [UInt] (generic index) ~ [UInt] (generic index)
     */
    SIMPLE_WITH_INDEX_2 {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor {
            val firstIndex = reader.readUInt()
            val secondIndex = reader.readUInt()
            visitor.visitIndices(op, firstIndex, secondIndex)
            return visitor
        }
    },

    /**
     * `inst` ~ list([UInt]) ~ [UInt]
     */
    BRANCH_TABLE {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor {
            val branches = reader.readList { it.readUInt() }
            val fallback = reader.readUInt()
            visitor.visitBranchTable(op, branches, fallback)
            return visitor
        }
    },

    /**
     * `inst` ~ [Byte] ~ [UInt] (label) ~ [HeapType] ~ [HeapType]
     */
    BRANCH_ON_CAST {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor {
            val byte = reader.readByte()
            val label = reader.readUInt()
            val from = HeapType.decode(reader)
            val to = HeapType.decode(reader)

            visitor.visitBranchOnCast(
                op,
                label,
                from = when (byte.hasBit(0)) {
                    true -> RefType.Nullable(from)
                    false -> RefType.NonNull(from)
                },
                to = when (byte.hasBit(1)) {
                    true -> RefType.Nullable(to)
                    false -> RefType.NonNull(to)
                }
            )

            return visitor
        }
    },

    /**
     * Special opcodes to control blocks.
     */
    BLOCK_CONTROL {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor? {
            when (op) {
                Op.END -> {
                    visitor.visitEnd()
                    return parent
                }

                Op.ELSE if visitor is IfBlockVisitor -> {
                    return visitor.visitElse()
                }

                else -> throw InvalidModuleDataException("This should never be called with a non block control Op. Did a new one get added?")
            }
        }

        override fun shouldPushCurrentBlock(op: Op): Boolean {
            return false
        }
    },

    LEGACY_EXCEPTIONS {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor? {
            when (op) {
                Op.LEGACY_TRY -> {
                    if (visitor !is LegacyExceptionsVisitor)
                        throw LegacyExceptionsUnsupportedException("The provided visitor is not capable of processing legacy exceptions instructions, yet there are such instructions present in the provided code")

                    val blockType = BlockType.decode(reader)
                    return visitor.visitLegacyTry(blockType)
                }

                Op.LEGACY_CATCH -> {
                    if(visitor !is LegacyTryBlockVisitor)
                        throw InvalidModuleDataException("Legacy catch instructions must follow a try block and precede catch_all")

                    val tagIndex = reader.readUInt()
                    return visitor.visitCatch(tagIndex)
                }

                Op.LEGACY_CATCH_ALL -> {
                    if(visitor !is LegacyTryBlockVisitor)
                        throw InvalidModuleDataException("Legacy catch instructions must follow a try block and precede catch_all")

                    return visitor.visitCatchAll()
                }

                Op.LEGACY_TRY_DELEGATE -> {
                    if(visitor !is LegacyTryBlockVisitor)
                        throw InvalidModuleDataException("Legacy catch instructions must follow a try block and precede catch_all")

                    val label = reader.readUInt()
                    visitor.visitDelegate(label)
                    return parent
                }

                else -> throw InvalidModuleDataException("This should never be called with a non legacy exceptions Op. Did a new one get added?")
            }
        }

        override fun shouldPushCurrentBlock(op: Op): Boolean {
            return op == Op.LEGACY_TRY
        }
    },

    /**
     * `inst` ~ [BlockType] ~ ([Op])* ~ [Op.END]
     */
    BLOCK_START {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor {
            val blockType = BlockType.decode(reader)
            return visitor.visitBlock(op, blockType)
        }
    },

    /**
     * `inst` ~ [BlockType] ~ ([Op])* ~ ([Op.ELSE] ~ ([Op])*)? ~ [Op.END]
     */
    BLOCK_IF {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor {
            val blockType = BlockType.decode(reader)
            return visitor.visitIf(op, blockType)
        }
    },

    /**
     * `inst` ~ [BlockType] ~ list([Catch]) ~ ([Op])* ~ [Op.END]
     */
    CATCH_TABLE {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor {
            val blockType = BlockType.decode(reader)
            val catches = reader.readList { Catch.decode(it) }
            return visitor.visitCatchTable(op, blockType, catches)
        }
    },

    /**
     * `inst` ~ [MemArg]
     */
    MEMARG {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor {
            val memArg = MemArg.decode(reader)
            visitor.visitMemoryOp(op, memArg)
            return visitor
        }
    },

    /**
     * `inst` ~ [MemArg] ~ [Byte]
     */
    MEMARG_LANE {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor {
            val memArg = MemArg.decode(reader)
            val laneIndex = reader.readByte()
            visitor.visitMemoryOpWithLane(op, memArg, laneIndex)
            return visitor
        }
    },

    /**
     * `inst` ~ [HeapType]
     */
    HEAP_TYPE {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor {
            val heapType = HeapType.decode(reader)
            visitor.visitWithHeapType(op, heapType)
            return visitor
        }
    },

    /**
     * `inst` ~ [Int]
     */
    CONST_I32 {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor {
            val value = reader.readInt()
            visitor.visitConstI32(op, value)
            return visitor
        }
    },

    /**
     * `inst` ~ [Long]
     */
    CONST_I64 {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor {
            val value = reader.readLong()
            visitor.visitConstI64(op, value)
            return visitor
        }
    },

    /**
     * `inst` ~ [Float]
     */
    CONST_F32 {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor {
            val value = reader.readFloat()
            visitor.visitConstF32(op, value)
            return visitor
        }
    },

    /**
     * `inst` ~ [Double]
     */
    CONST_F64 {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor {
            val value = reader.readDouble()
            visitor.visitConstF64(op, value)
            return visitor
        }
    },

    /**
     * `inst` ~ [VectorValue]
     */
    CONST_V128 {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor {
            val value = VectorValue(reader)
            visitor.visitConstV128(op, value)
            return visitor
        }
    },

    /**
     * `inst` ~ [Byte]
     */
    LANE_INDEX {
        override fun visit(
            reader: WasmReader,
            op: Op,
            parent: InstructionVisitor?,
            visitor: InstructionVisitor
        ): InstructionVisitor {
            val laneIndex = reader.readByte()
            visitor.visitWithLane(op, laneIndex)
            return visitor
        }
    };

    abstract fun visit(
        reader: WasmReader,
        op: Op,
        parent: InstructionVisitor?,
        visitor: InstructionVisitor
    ): InstructionVisitor?

    open fun shouldPushCurrentBlock(op: Op): Boolean {
        return true
    }
}
