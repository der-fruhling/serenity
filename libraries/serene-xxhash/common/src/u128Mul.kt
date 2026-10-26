package net.derfruhling.serene.xxhash

// https://stackoverflow.com/questions/25095741/how-can-i-multiply-64-bit-operands-and-get-128-bit-result-portably
internal inline fun <T> u128Mul(a: ULong, b: ULong, fn: (hi: ULong, lo: ULong) -> T): T {
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

    return fn(hi, lo)
}
