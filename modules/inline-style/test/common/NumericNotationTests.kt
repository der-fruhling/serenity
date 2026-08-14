package net.derfruhling.serenity.tests.style

import net.derfruhling.serenity.style.UnparsableNotationException
import net.derfruhling.serenity.style.notations.notation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NumericNotationTests {
    @Test
    fun `can parse int`() {
        assertEquals(42, Int.notation.fromNotationString("42"))
        assertEquals(22, Int.notation.fromNotationString("+22"))
        assertEquals(-88, Int.notation.fromNotationString("-88"))
    }

    @Test
    fun `can emit int`() {
        assertEquals("42", Int.notation.asNotationString(42))
        assertEquals("-88", Int.notation.asNotationString(-88))
    }

    @Test
    fun `int cannot accept floating points`() {
        assertFailsWith<UnparsableNotationException> {
            Int.notation.fromNotationString("42.7")
        }

        assertFailsWith<UnparsableNotationException> {
            Int.notation.fromNotationString("-88.42")
        }
    }

    @Test
    fun `int cannot accept exponents`() {
        assertFailsWith<UnparsableNotationException> {
            Int.notation.fromNotationString("42e7")
        }

        assertFailsWith<UnparsableNotationException> {
            Int.notation.fromNotationString("-98e4")
        }

        assertFailsWith<UnparsableNotationException> {
            Int.notation.fromNotationString("-98e-4")
        }
    }

    @Test
    fun `can parse float`() {
        assertEquals(42.7f, Float.notation.fromNotationString("42.7"), TOL_F)
        assertEquals(-22.7f, Float.notation.fromNotationString("-22.7"), TOL_F)
    }

    @Test
    fun `can emit float`() {
        assertEquals("42.7", Float.notation.asNotationString(42.7f))
        assertEquals("-22.7", Float.notation.asNotationString(-22.7f))
    }

    @Test
    fun `can parse double`() {
        assertEquals(42.7, Double.notation.fromNotationString("42.7"), TOL)
        assertEquals(-22.7, Double.notation.fromNotationString("-22.7"), TOL)
    }

    @Test
    fun `can emit double`() {
        assertEquals("42.7", Double.notation.asNotationString(42.7))
        assertEquals("-22.7", Double.notation.asNotationString(-22.7))
    }
}