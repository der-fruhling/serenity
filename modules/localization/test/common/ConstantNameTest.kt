package net.derfruhling.serenity.test.localization

import net.derfruhling.serenity.localization.n
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

class ConstantNameTest {
    private fun rt(name: String) = n(name)

    @Test
    fun testConstantNameWorks() {
        assertEquals(0x5b98a8fcb057bbf1, n(STRING_WORKS).asLong)
        assertEquals(0x5b98a8fcb057bbf1, rt(STRING_WORKS).asLong)
    }

    @Test
    fun testConstantName3Char() {
        assertEquals(0x7d7f05f5786c07c5, n(STRING_3CHAR).asLong)
        assertEquals(0x7d7f05f5786c07c5, rt(STRING_3CHAR).asLong)
    }

    @Test
    fun testConstantName15Char() {
        assertEquals(0xbdedbb00082d3658u, n(STRING_15CHAR).asULong)
        assertEquals(0xbdedbb00082d3658u, rt(STRING_15CHAR).asULong)
    }

    @Test
    fun testConstantName39Char() {
        assertEquals(0x77dd0f8f6ef7bade, n(STRING_39CHAR).asLong)
        assertEquals(0x77dd0f8f6ef7bade, rt(STRING_39CHAR).asLong)
    }

    @Test
    fun testConstantName168Char() {
        assertEquals(0xf3160b4d04a9ce0cu, n(STRING_168CHAR).asULong)
        assertEquals(0xf3160b4d04a9ce0cu, rt(STRING_168CHAR).asULong)
    }

    @Test
    fun testConstantName367Char() {
        assertEquals(0x76080ad0fbecde37u, n(STRING_367CHAR).asULong)
        assertEquals(0x76080ad0fbecde37u, rt(STRING_367CHAR).asULong)
    }
}
