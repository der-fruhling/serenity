package net.derfruhling.serene.xxhash.tests

import net.derfruhling.serene.xxhash.xxh3
import kotlin.test.Test
import kotlin.test.assertEquals

private const val STRING_WORKS = "yeppers peppers"
private const val STRING_3CHAR = "yep"
private const val STRING_15CHAR = "according to me"
private const val STRING_39CHAR = "according to all known laws of aviation"

private const val STRING_168CHAR =
    "i mean, i **could** paste the entire bee movie script in here, but today we're really concerned about lawsuits and intellectual property for whatever reason, so no dice"

private const val STRING_367CHAR =
    "i mean, i **could** paste the entire bee movie script in here, but today we're really concerned about lawsuits and intellectual property for whatever reason, so no dice. instead, we could generate a bunch of characters of lorem ipsum with some kind of online lorem ipsum generator, but where's the fun in that? really? you used a website to generate lorem ipsum? why?"

class Xxh3Test {
    private fun rt(name: String) = xxh3(name)

    @Test
    fun testXxh3Works() {
        assertEquals(0x5b98a8fcb057bbf1u, rt(STRING_WORKS).asULong)
    }

    @Test
    fun test3Char() {
        assertEquals(0x7d7f05f5786c07c5u, rt(STRING_3CHAR).asULong)
    }

    @Test
    fun test15Char() {
        assertEquals(0xbdedbb00082d3658u, rt(STRING_15CHAR).asULong)
    }

    @Test
    fun test39Char() {
        assertEquals(0x77dd0f8f6ef7badeu, rt(STRING_39CHAR).asULong)
    }

    @Test
    fun test168Char() {
        assertEquals(0xf3160b4d04a9ce0cu, rt(STRING_168CHAR).asULong)
    }

    @Test
    fun test367Char() {
        assertEquals(0x76080ad0fbecde37u, rt(STRING_367CHAR).asULong)
    }
}
