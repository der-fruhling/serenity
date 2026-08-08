package net.derfruhling.serene.wasm

import kotlinx.io.*
import kotlinx.io.bytestring.ByteString
import net.derfruhling.serene.wasm.instruction.Op
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.jvm.JvmInline
import kotlin.math.sign

@JvmInline
value class WasmWriter(private val sink: Sink) {
    constructor(rawSink: RawSink) : this(rawSink.buffered())

    fun writeByte(byte: Byte) {
        sink.writeByte(byte)
    }

    fun writeBytes(bytes: ByteArray) = sink.write(bytes)
    fun writeBytes(bytes: RawSource, byteCount: Long) = sink.write(bytes, byteCount)
    fun writeBytes(bytes: Buffer, byteCount: Long = bytes.size) = sink.write(bytes, byteCount)
    fun writeBytes(bytes: ByteString) = sink.write(bytes)

    fun writeGenericInt(fn: suspend SequenceScope<Byte>.() -> Unit) {
        var lastByte: Byte? = null
        iterator(fn).forEach { b ->
            if (lastByte != null) {
                writeByte(lastByte or 0x80.toByte())
            }

            lastByte = b
        }

        writeByte(lastByte ?: 0)
    }

    fun writeUShort(value: UShort) = writeInt(value.toInt())
    fun writeUInt(value: UInt) = writeGenericInt {
        var v = value

        do {
            yield((v and 0x7Fu).toByte())
            v = v shr 7
        } while (v != 0u)
    }

    fun writeULong(value: ULong) = writeGenericInt {
        var v = value

        do {
            yield((v and 0x7Fu).toByte())
            v = v shr 7
        } while (v != 0uL)
    }

    fun writeShort(value: Short) = writeInt(value.toInt())

    fun writeStaticUInt(value: UInt) = sink.writeUIntLe(value)
    fun writeMagicUInt(value: UInt) = sink.writeUInt(value)

    fun writeInt(value: Int) = writeGenericInt {
        var v = value
        do {
            var byte = (v and 0x7f).toByte()
            v = v shr 7
            val signBit = byte and 0x40
            val done = (v == 0 && signBit == 0.toByte()) || (v == -1 && signBit != 0.toByte())
            if(!done) byte = byte or 0x80.toByte()
            yield(byte)
        } while(!done)
    }

    fun writeLong(value: Long) = writeGenericInt {
        var v = value
        do {
            var byte = (v and 0x7f).toByte()
            v = v shr 7
            val signBit = byte and 0x40
            val done = (v == 0L && signBit == 0.toByte()) || (v == -1L && signBit != 0.toByte())
            if(!done) byte = byte or 0x80.toByte()
            yield(byte)
        } while(!done)
    }

    fun writeFloat(value: Float) = sink.writeFloatLe(value)
    fun writeDouble(value: Double) = sink.writeDoubleLe(value)

    fun writeString(string: String) {
        val bytes = string.encodeToByteArray()
        writeUInt(bytes.size.toUInt())
        sink.write(bytes)
    }

    inline fun <T> writeList(items: Collection<T>, fn: WasmWriter.(T) -> Unit) {
        writeUInt(items.size.toUInt())

        for (item in items) {
            fn(item)
        }
    }

    fun <T : Encode> writeList(items: Collection<T>) {
        writeList(items) { it.encode(this) }
    }

    fun writeOp(op: Op) {
        writeByte(op.opcode.toByte())

        if (op.ext >= 0) {
            writeInt(op.ext)
        }
    }
}
