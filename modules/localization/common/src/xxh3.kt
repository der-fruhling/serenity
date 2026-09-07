@file:OptIn(ExperimentalUnsignedTypes::class)

package net.derfruhling.serenity.localization

// https://github.com/Cyan4973/xxHash/blob/dev/doc/xxhash_spec.md#xxh3-algorithm-overview

class XXH3 private constructor(val seed: Long, internal val secret: ByteArray) {
    private val xxh3b64f4to8Seed by lazy { seed xor bswap((seed and 0xFFFFFFFF).toUInt()).toLong() }

    private val xxh3b64Empty by lazy {
        val x1 = secret.getLong(56)
        val x2 = secret.getLong(64)
        avalancheXXH64(seed xor x1 xor x2)
    }

    private fun xxh3b64f1to3(combined: UInt): Long {
        val x1 = secret.getInt(0)
        val x2 = secret.getInt(4)
        val value = ((x1 xor x2).toULong() + seed.toULong()) xor combined.toULong()
        return avalancheXXH64(value.toLong())
    }

    private fun xxh3b64f1to3(bytes: ByteArray): Long {
        val combined = bytes[bytes.size - 1].toUInt() or
            (bytes.size.toUInt() shl 8) or
            (bytes[0].toUInt() shl 16) or
            (bytes[bytes.size ushr 1].toUInt() shl 24)

        return xxh3b64f1to3(combined)
    }

    private fun xxh3b64f4to8(inputFirst: UInt, inputLast: UInt, inputLength: UInt): Long {
        val x1 = secret.getLong(8)
        val x2 = secret.getLong(16)
        val modifiedSeed = xxh3b64f4to8Seed
        val combined = inputLast.toULong() or (inputFirst.toULong() shl 32)
        var value = ((x1 xor x2).toULong() - modifiedSeed.toULong()) xor combined
        value = value xor (value shl 49) xor (value shl 24)
        value *= PRIME_MX2
        value = value xor ((value shr 35) + inputLength)
        value *= PRIME_MX2
        value = value xor (value shr 28)
        return value.toLong()
    }

    private fun xxh3b64f4to8(bytes: ByteArray): Long {
        val inputFirst = bytes.getInt(0).toUInt()
        val inputLast = bytes.getInt(bytes.size - 4).toUInt()
        return xxh3b64f4to8(inputFirst, inputLast, bytes.size.toUInt())
    }

    private fun xxh3b64f9to16(inputFirst: ULong, inputLast: ULong, inputLength: UInt): Long {
        val x1 = secret.getLong(24)
        val x2 = secret.getLong(32)
        val x3 = secret.getLong(40)
        val x4 = secret.getLong(48)
        val low = ((x1 xor x2).toULong() + seed.toULong()) xor inputFirst
        val high = ((x3 xor x4).toULong() - seed.toULong()) xor inputLast

        val (mh, ml) = u128Mul(low, high)
        val value = inputLength + bswap(low) + high + (ml xor mh)
        return avalanche(value.toLong())
    }

    private fun xxh3b64f9to16(bytes: ByteArray): Long {
        val inputFirst = bytes.getLong(0).toULong()
        val inputLast = bytes.getLong(bytes.size - 8).toULong()
        return xxh3b64f9to16(inputFirst, inputLast, bytes.size.toUInt())
    }

    private fun xxh3b64Small(bytes: ByteArray): Long {
        return when (bytes.size) {
            0 -> xxh3b64Empty
            in 1..3 -> xxh3b64f1to3(bytes)
            in 4..8 -> xxh3b64f4to8(bytes)
            in 9..16 -> xxh3b64f9to16(bytes)
            else -> throw NotImplementedError()
        }
    }

    private inner class MediumAccum(inputLength: Int) {
        var acc: ULong = inputLength.toULong() * PRIME64_1

        fun mixStep(data: ByteArray, offset: Int, secretOffset: Int, seed: Long): ULong {
            val d1 = data.getLong(offset).toULong()
            val d2 = data.getLong(offset + 8).toULong()
            val x1 = secret.getLong(secretOffset).toULong()
            val x2 = secret.getLong(secretOffset + 8).toULong()
            val (mh, ml) = u128Mul(d1 xor (x1 + seed.toULong()), d2 xor (x2 - seed.toULong()))
            return ml xor mh
        }
    }

    private inline fun xxh3medium(inputLength: Int, fn: MediumAccum.() -> Long): Long {
        return MediumAccum(inputLength).fn()
    }

    private fun xxh3b64f17to128(bytes: ByteArray) = xxh3medium(bytes.size) {
        val numRounds = ((bytes.size - 1) ushr 5) + 1

        for(i in 0..<numRounds) {
            val start = i * 16
            val end = bytes.size - (i * 16) - 16;
            acc += mixStep(bytes, start, i * 32, seed)
            acc += mixStep(bytes, end, i * 32 + 16, seed)
        }

        avalanche(acc.toLong())
    }

    private fun xxh3b64f129to240(bytes: ByteArray) = xxh3medium(bytes.size) {
        val numChunks = bytes.size ushr 5

        for(i in 0..<8) {
            acc += mixStep(bytes, i * 16, i * 16, seed)
        }

        acc = avalanche(acc.toLong()).toULong()

        for(i in 8..<numChunks) {
            acc += mixStep(bytes, i * 16, (i - 8) * 16 + 3, seed)
        }

        acc += mixStep(bytes, bytes.size - 16, 119, seed)
        avalanche(acc.toLong())
    }

    private fun xxh3b64Medium(bytes: ByteArray): Long {
        return when(bytes.size) {
            in 17..128 -> xxh3b64f17to128(bytes)
            in 129..240 -> xxh3b64f129to240(bytes)
            else -> throw NotImplementedError()
        }
    }

    private val stripesPerBlock by lazy { (secret.size - 64) / 8 }
    private val blockSize by lazy { 64 * stripesPerBlock }

    private inner class LargeAccum {
        val acc: ULongArray = ulongArrayOf(
            PRIME32_3,
            PRIME64_1,
            PRIME64_2,
            PRIME64_3,
            PRIME64_4,
            PRIME32_2,
            PRIME64_5,
            PRIME32_1
        )

        fun accumulate(stripeBytes: ByteArray, offset: Int, secretOffset: Int) {
            for(i in 0..<8) {
                val stripe = stripeBytes.getLong(offset + i * 8).toULong()
                val secret = secret.getLong(secretOffset + i * 8).toULong()
                val value = stripe xor secret
                acc[i xor 1] += stripe
                acc[i] += (value and 0xFFFFFFFFu) * (value shr 32)
            }
        }

        fun round(block: ByteArray, offset: Int) {
            fun roundAccumulate(block: ByteArray, offset: Int) {
                for(i in 0..<stripesPerBlock) {
                    accumulate(block, offset + i * 64, i * 8)
                }
            }

            fun roundScramble() {
                val secretOffset = secret.size - 64
                for(i in 0..<8) {
                    val secret = secret.getLong(secretOffset + i * 8).toULong()
                    acc[i] = acc[i] xor (acc[i] shr 47)
                    acc[i] = acc[i] xor secret
                    acc[i] = acc[i] * PRIME32_1
                }
            }

            roundAccumulate(block, offset)
            roundScramble()
        }

        fun repeatRounds(bytes: ByteArray): Int {
            val blockSize = blockSize
            var offset = 0

            while(offset < bytes.size - blockSize) {
                round(bytes, offset)
                offset += blockSize
            }

            return offset
        }

        fun lastRound(block: ByteArray, offset: Int, lastStripeOffset: Int) {
            val numFullStripes = (block.size - 1) / 64

            for(i in 0..<numFullStripes) {
                accumulate(block, offset + i * 64, i * 8)
            }

            accumulate(block, lastStripeOffset, secret.size - 71)
        }

        fun finalMerge(initValue: ULong, secretOffset: Int): Long {
            val secretWords = ULongArray(8) { secret.getLong(secretOffset + it * 8).toULong() }
            var result: ULong = initValue

            repeat(4) { i ->
                val (mh, ml) = u128Mul(acc[i * 2] xor secretWords[i * 2], acc[i * 2 + 1] xor secretWords[i * 2 + 1])
                result += ml xor mh
            }

            return avalanche(result.toLong())
        }
    }

    private fun xxh3b64Large(bytes: ByteArray) = LargeAccum().run {
        val lastBlockOffset = repeatRounds(bytes)
        lastRound(bytes, lastBlockOffset, bytes.size - 64)
        finalMerge(bytes.size.toULong() * PRIME64_1, 11)
    }

    fun digest(bytes: ByteArray): Long = when {
        bytes.size <= 16 -> xxh3b64Small(bytes)
        bytes.size in 17..240 -> xxh3b64Medium(bytes)
        else -> xxh3b64Large(bytes)
    }

    companion object {
        private val default by lazy { XXH3(0, defaultSecret) }
        private fun fromSeedLarge(seed: Long) = XXH3(seed, deriveSecret(seed))
        private fun fromSeed(seed: Long) = XXH3(seed, defaultSecret)

        fun digest(bytes: ByteArray): Long = default.digest(bytes)
        fun digestWithSeed(bytes: ByteArray, seed: Long): Long = when {
            bytes.size <= 240 -> fromSeed(seed)
            else -> fromSeedLarge(seed)
        }.digest(bytes)

        private const val PRIME32_1: ULong = 0x9E3779B1UL  // 0b10011110001101110111100110110001
        private const val PRIME32_2: ULong = 0x85EBCA77UL  // 0b10000101111010111100101001110111
        private const val PRIME32_3: ULong = 0xC2B2AE3DUL  // 0b11000010101100101010111000111101
        private const val PRIME64_1: ULong =
            0x9E3779B185EBCA87UL  // 0b1001111000110111011110011011000110000101111010111100101010000111
        private const val PRIME64_2: ULong =
            0xC2B2AE3D27D4EB4FUL  // 0b1100001010110010101011100011110100100111110101001110101101001111
        private const val PRIME64_3: ULong =
            0x165667B19E3779F9UL  // 0b0001011001010110011001111011000110011110001101110111100111111001
        private const val PRIME64_4: ULong =
            0x85EBCA77C2B2AE63UL  // 0b1000010111101011110010100111011111000010101100101010111001100011
        private const val PRIME64_5: ULong =
            0x27D4EB2F165667C5UL  // 0b0010011111010100111010110010111100010110010101100110011111000101
        private const val PRIME_MX1: ULong =
            0x165667919E3779F9UL  // 0b0001011001010110011001111001000110011110001101110111100111111001
        private const val PRIME_MX2: ULong =
            0x9FB21C651E98DF25UL  // 0b1001111110110010000111000110010100011110100110001101111100100101

        private val defaultSecret = intArrayOf(
            0xb8, 0xfe, 0x6c, 0x39, 0x23, 0xa4, 0x4b, 0xbe, 0x7c, 0x01, 0x81, 0x2c, 0xf7, 0x21, 0xad, 0x1c,
            0xde, 0xd4, 0x6d, 0xe9, 0x83, 0x90, 0x97, 0xdb, 0x72, 0x40, 0xa4, 0xa4, 0xb7, 0xb3, 0x67, 0x1f,
            0xcb, 0x79, 0xe6, 0x4e, 0xcc, 0xc0, 0xe5, 0x78, 0x82, 0x5a, 0xd0, 0x7d, 0xcc, 0xff, 0x72, 0x21,
            0xb8, 0x08, 0x46, 0x74, 0xf7, 0x43, 0x24, 0x8e, 0xe0, 0x35, 0x90, 0xe6, 0x81, 0x3a, 0x26, 0x4c,
            0x3c, 0x28, 0x52, 0xbb, 0x91, 0xc3, 0x00, 0xcb, 0x88, 0xd0, 0x65, 0x8b, 0x1b, 0x53, 0x2e, 0xa3,
            0x71, 0x64, 0x48, 0x97, 0xa2, 0x0d, 0xf9, 0x4e, 0x38, 0x19, 0xef, 0x46, 0xa9, 0xde, 0xac, 0xd8,
            0xa8, 0xfa, 0x76, 0x3f, 0xe3, 0x9c, 0x34, 0x3f, 0xf9, 0xdc, 0xbb, 0xc7, 0xc7, 0x0b, 0x4f, 0x1d,
            0x8a, 0x51, 0xe0, 0x4b, 0xcd, 0xb4, 0x59, 0x31, 0xc8, 0x9f, 0x7e, 0xc9, 0xd9, 0x78, 0x73, 0x64,
            0xea, 0xc5, 0xac, 0x83, 0x34, 0xd3, 0xeb, 0xc3, 0xc5, 0x81, 0xa0, 0xff, 0xfa, 0x13, 0x63, 0xeb,
            0x17, 0x0d, 0xdd, 0x51, 0xb7, 0xf0, 0xda, 0x49, 0xd3, 0x16, 0x55, 0x26, 0x29, 0xd4, 0x68, 0x9e,
            0x2b, 0x16, 0xbe, 0x58, 0x7d, 0x47, 0xa1, 0xfc, 0x8f, 0xf8, 0xb8, 0xd1, 0x7a, 0xd0, 0x31, 0xce,
            0x45, 0xcb, 0x3a, 0x8f, 0x95, 0x16, 0x04, 0x28, 0xaf, 0xd7, 0xfb, 0xca, 0xbb, 0x4b, 0x40, 0x7e,
        ).map { it.toByte() }.toByteArray()

        private val defaultSecretLongs: LongArray by lazy {
            LongArray(24) {
                val offset = it * 8
                defaultSecret.getLong(offset)
            }
        }

        private fun ByteArray.getLong(offset: Int): Long {
            var long = this[offset].toLong()
            long = long or (this[offset + 1].toLong() shl 8)
            long = long or (this[offset + 2].toLong() shl 16)
            long = long or (this[offset + 3].toLong() shl 24)
            long = long or (this[offset + 4].toLong() shl 32)
            long = long or (this[offset + 5].toLong() shl 40)
            long = long or (this[offset + 6].toLong() shl 48)
            long = long or (this[offset + 7].toLong() shl 56)
            return long
        }

        private fun ByteArray.getInt(offset: Int): Int {
            var int = this[offset].toInt()
            int = int or (this[offset + 1].toInt() shl 8)
            int = int or (this[offset + 2].toInt() shl 16)
            int = int or (this[offset + 3].toInt() shl 24)
            return int
        }

        private fun bswap(value: UInt): UInt {
            return ((value and 0x000000FFu) shl 24) +
                ((value and 0x0000FF00u) shl 8) +
                ((value and 0x00FF0000u) shr 8) +
                ((value and 0xFF000000u) shr 24)
        }

        private fun bswap(value: ULong): ULong {
            return ((value and 0x00000000000000FFu) shl 56) +
                ((value and 0x000000000000FF00u) shl 40) +
                ((value and 0x0000000000FF0000u) shl 24) +
                ((value and 0x00000000FF000000u) shl 8) +
                ((value and 0x000000FF00000000u) shr 8) +
                ((value and 0x0000FF0000000000u) shr 24) +
                ((value and 0x00FF000000000000u) shr 40) +
                ((value and 0xFF00000000000000u) shr 56)
        }

        private fun deriveSecretLong(seed: Long): LongArray {
            return defaultSecretLongs.copyOf().also {
                for (i in 0..<12) {
                    it[i * 2] = (it[i * 2].toULong() + seed.toULong()).toLong()
                    it[i * 2 + 1] = (it[i * 2 + 1].toULong() - seed.toULong()).toLong()
                }
            }
        }

        private fun deriveSecret(seed: Long): ByteArray {
            val longs = deriveSecretLong(seed)
            return ByteArray(192) {
                val long = longs[it shr 3]
                val byte = (long ushr ((it and 7) * 8)).toByte()
                byte
            }
        }

        private fun avalanche(target: Long): Long {
            var x = target
            x = x xor (x ushr 37)
            x = (x.toULong() * PRIME_MX1).toLong()
            x = x xor (x ushr 32)
            return x
        }

        private fun avalancheXXH64(target: Long): Long {
            var x = target
            x = x xor (x ushr 33)
            x = (x.toULong() * PRIME64_2).toLong()
            x = x xor (x ushr 29)
            x = (x.toULong() * PRIME64_3).toLong()
            x = x xor (x ushr 33)
            return x
        }

        // https://stackoverflow.com/questions/25095741/how-can-i-multiply-64-bit-operands-and-get-128-bit-result-portably
        private fun u128Mul(a: ULong, b: ULong): Pair<ULong, ULong> {
            val u1 = a and 0xFFFFFFFFu
            val v1 = b and 0xFFFFFFFFu
            var t = u1 * v1
            val w3 = t and 0xFFFFFFFFu
            var k = t shr 32

            val op1 = a shr 32
            t = (op1 * v1) + k
            k = (t and 0xFFFFFFFFu)
            val w1 = t shr 32

            val op2 = b shr 32
            t = (u1 * op2) + k
            k = (t shr 32)

            val hi = (op1 * op2) + w1 + k
            val lo = (t shl 32) + w3

            return hi to lo
        }
    }
}
