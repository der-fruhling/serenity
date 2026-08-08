package net.derfruhling.serene.wasm

import kotlinx.io.*
import kotlinx.io.bytestring.ByteString
import net.derfruhling.serene.wasm.instruction.InstructionVisitor
import net.derfruhling.serene.wasm.instruction.Op
import net.derfruhling.serene.wasm.instruction.UnknownInstructionException
import net.derfruhling.serene.wasm.instruction.WasmWriterInstructionVisitor
import net.derfruhling.serene.wasm.module.InvalidModuleDataException
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.reflect.KProperty

class PositionKeeper private constructor(var position: Long) {
    constructor() : this(0)

    fun inherit() = PositionKeeper(position)

    operator fun getValue(self: Any?, property: KProperty<*>): Long {
        return position
    }

    operator fun setValue(self: Any?, property: KProperty<*>, value: Long) {
        require(value >= position)
        position = value
    }
}

class WasmReader(private val source: Source, internal val keeper: PositionKeeper = PositionKeeper()) {
    constructor(rawSource: RawSource) : this(rawSource.buffered())
    constructor(bytes: ByteArray) : this(Buffer().also { it.write(bytes) })

    var bytesRead by keeper
    val isExhausted
        get() = source.exhausted()

    fun advanceTo(other: WasmReader) {
        require(other.keeper.position >= this.keeper.position)
        source.skip(other.keeper.position - this.keeper.position)
        this.keeper.position = other.keeper.position
    }

    fun peek() = WasmReader(source.peek(), keeper.inherit())

    fun readByte(): Byte {
        bytesRead++
        return source.readByte()
    }

    fun consume(): ByteString {
        return source.readByteString().also { bytesRead += it.size }
    }

    fun readBytes(length: Long): Buffer {
        val buffer = Buffer()
        source.readTo(buffer, length)
        bytesRead += length
        return buffer
    }

    fun readBytes(length: Int) = readBytes(length.toLong())
    fun readBytes(length: UInt) = readBytes(length.toLong())

    inline fun readUntil(condition: (Byte) -> Boolean): Buffer {
        val buffer = Buffer()

        do {
            val byte = readByte()
            buffer.writeByte(byte)
        } while(!condition(byte))

        return buffer
    }

    @PublishedApi
    internal inline fun <T> commonGenericIntRead(initial: T, accum: (T, Byte, Int) -> T): Triple<T, Int, Byte> {
        var value = initial
        val bytes = readUntil { !it.hasBit(7) }

        var offset = 0
        var byte: Byte
        do {
            byte = bytes.readByte()
            value = accum(value, byte and 0x7F, offset)
            offset += 7
        } while (!bytes.exhausted())
        return Triple(value, offset, byte)
    }

    inline fun <T> readGenericInt(initial: T, accum: (T, Byte, Int) -> T): T {
        val (value) = commonGenericIntRead(initial, accum)
        return value
    }

    inline fun <T> readGenericSignedInt(initial: T, signExtend: (T, Int) -> T, accum: (T, Byte, Int) -> T): T {
        val (value, offset, byte) = commonGenericIntRead(initial, accum)
        return if (byte.hasBit(6)) {
            signExtend(value, offset)
        } else {
            value
        }
    }

    fun readUInt() = readGenericInt(0u) { acc, byte, off ->
        if(off > 32) throw InvalidModuleDataException("ULEB128 integer too long")
        acc or (byte.toUInt() shl off)
    }

    fun readULong() = readGenericInt(0uL) { acc, byte, off ->
        if(off > 64) throw InvalidModuleDataException("ULEB128 integer too long")
        acc or (byte.toULong() shl off)
    }

    fun readInt() = readGenericSignedInt(0, { v, off ->
        // sign extension
        if(off < 32) v or (0.inv() shl off) else v
    }) { acc, byte, off ->
        if(off > 32) throw InvalidModuleDataException("SLEB128 integer too long")
        acc or (byte.toInt() shl off)
    }

    fun readLong() = readGenericSignedInt(0L, { v, off ->
        // sign extension
        if(off < 64) v or (0L.inv() shl off) else v
    }) { acc, byte, off ->
        if(off > 64) throw InvalidModuleDataException("SLEB128 integer too long")
        acc or (byte.toLong() shl off)
    }

    fun readStaticUShort(): UShort {
        bytesRead += 2
        return source.readUShortLe()
    }

    fun readStaticUInt(): UInt {
        bytesRead += 4
        return source.readUIntLe()
    }

    fun readStaticULong(): ULong {
        bytesRead += 8
        return source.readULongLe()
    }

    fun readStaticShort(): Short {
        bytesRead += 2
        return source.readShortLe()
    }

    fun readStaticInt(): Int {
        bytesRead += 4
        return source.readIntLe()
    }

    fun readStaticLong(): Long {
        bytesRead += 8
        return source.readLongLe()
    }

    fun readFloat(): Float {
        bytesRead += 4
        return source.readFloatLe()
    }

    fun readDouble(): Double {
        bytesRead += 8
        return source.readDoubleLe()
    }

    fun readString(): String {
        val byteCount = readUInt()
        val bytes = readBytes(byteCount)

        return bytes.readString()
    }

    fun <T> readList(fn: (WasmReader) -> T): List<T> {
        return List(readUInt().toInt()) { fn(this) }
    }

    fun readExpr(): CodeBlob {
        try {
            val buffer = Buffer()
            val writerVisitor = WasmWriterInstructionVisitor(WasmWriter(buffer))
            var currentVisitor: InstructionVisitor = writerVisitor
            val blockStack = arrayListOf<InstructionVisitor>()

            while (true) {
                val op = readOp()
                val newVisitor = op.type.visit(this, op, blockStack.lastOrNull(), currentVisitor)
                    ?: break

                if(newVisitor !== currentVisitor) {
                    when(op) {
                        Op.END -> blockStack.removeLast()
                        Op.ELSE -> {}
                        else -> {
                            if(op.type.shouldPushCurrentBlock(op))
                                blockStack.add(currentVisitor)
                        }
                    }

                    currentVisitor = newVisitor
                }
            }

            return CodeBlob(buffer.readByteString())
        } catch(e: EOFException) {
            throw InvalidModuleDataException("Expression did not end correctly", e)
        }
    }

    fun readOp(): Op {
        val opByte = readByte().toUByte()
        return if(opByte in Op.extRange) {
            val extOps = Op.extOps[opByte]
                ?: throw UnknownInstructionException("Extension instruction 0x${opByte.toString(16)} is not known")
            val ext = readUInt()
            extOps[ext]
                ?: throw UnknownInstructionException("Instruction $ext in extension block ${opByte.toString(16)} is not known")
        } else {
            Op.stdOps[opByte]
                ?: throw UnknownInstructionException("Instruction ${opByte.toString(16)} is not known")
        }
    }

    fun readMagicUInt(): UInt = source.readUInt()
}
