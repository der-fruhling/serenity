package net.derfruhling.serenity.test.localization

import net.derfruhling.serenity.localization.n
import kotlin.test.Test
import kotlin.test.assertEquals

class ConstantNameTest {
    @Test
    fun testConstantNameWorks() {
        assertEquals(6600211057155226609L, n("yeppers peppers").asLong)
    }
}
