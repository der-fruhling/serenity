package net.derfruhling.serene.xxhash

fun interface ULongHashFunction {
    operator fun invoke(bytes: ByteArray): ULong
}
