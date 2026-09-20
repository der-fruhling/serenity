package net.derfruhling.serenity.test.localization

import net.derfruhling.serenity.localization.n
import kotlin.test.Test
import kotlin.test.assertEquals

class ConstantNameTest {
    private fun rt(name: String) = n(name)

    @Test
    fun testConstantNameWorks() {
        assertEquals(0x5b98a8fcb057bbf1, n("yeppers peppers").asLong)
        assertEquals(0x5b98a8fcb057bbf1, rt("yeppers peppers").asLong)
    }

    @Test
    fun testConstantName3Char() {
        assertEquals(0x7d7f05f5786c07c5, n("yep").asLong)
        assertEquals(0x7d7f05f5786c07c5, rt("yep").asLong)
    }

    @Test
    fun testConstantName15Char() {
        assertEquals(0xbdedbb00082d3658u, n("according to me").asULong)
        assertEquals(0xbdedbb00082d3658u, rt("according to me").asULong)
    }

    @Test
    fun testConstantName39Char() {
        assertEquals(0x77dd0f8f6ef7bade, n("according to all known laws of aviation").asLong)
        assertEquals(0x77dd0f8f6ef7bade, rt("according to all known laws of aviation").asLong)
    }

    @Test
    fun testConstantName168Char() {
        assertEquals(0xf3160b4d04a9ce0cu, n("i mean, i **could** paste the entire bee movie script in here, but today we're really concerned about lawsuits and intellectual property for whatever reason, so no dice").asULong)
        assertEquals(0xf3160b4d04a9ce0cu, rt("i mean, i **could** paste the entire bee movie script in here, but today we're really concerned about lawsuits and intellectual property for whatever reason, so no dice").asULong)
    }

    @Test
    fun testConstantName367Char() {
        assertEquals(0x76080ad0fbecde37u, n("i mean, i **could** paste the entire bee movie script in here, but today we're really concerned about lawsuits and intellectual property for whatever reason, so no dice. instead, we could generate a bunch of characters of lorem ipsum with some kind of online lorem ipsum generator, but where's the fun in that? really? you used a website to generate lorem ipsum? why?").asULong)
        assertEquals(0x76080ad0fbecde37u, rt("i mean, i **could** paste the entire bee movie script in here, but today we're really concerned about lawsuits and intellectual property for whatever reason, so no dice. instead, we could generate a bunch of characters of lorem ipsum with some kind of online lorem ipsum generator, but where's the fun in that? really? you used a website to generate lorem ipsum? why?").asULong)
    }
}
