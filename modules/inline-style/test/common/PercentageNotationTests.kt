package net.derfruhling.serenity.tests.style

import net.derfruhling.serenity.style.notations.Percentage
import kotlin.test.Test
import kotlin.test.assertEquals

class PercentageNotationTests {
    @Test
    fun `can parse percentages`() {
        val percentage = Percentage.notation.fromNotationString("42%")
        assertEquals(42.0f, percentage.actualValue, TOL_F)
    }

    @Test
    fun `can emit percentages`() {
        val percentage = Percentage.notation.asNotationString(Percentage(42f))
        assertEquals("42%", percentage)
    }
}