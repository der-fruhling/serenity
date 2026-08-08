package net.derfruhling.serene.wasm

import kotlin.experimental.and
import kotlin.experimental.or

@PublishedApi
internal fun Byte.hasBit(byte: Byte): Boolean {
    return (this and byte) != 0.toByte()
}

@Suppress("NOTHING_TO_INLINE")
@PublishedApi
internal inline fun Byte.hasBit(bit: Int) = hasBit((1 shl bit).toByte())

fun Byte.fixByte(): Byte {
    val b = this.toUByte()
    val signBit = b and 0x80u
    return (b and 0x3fu).toByte() or signBit.rotateRight(1).toByte()
}
