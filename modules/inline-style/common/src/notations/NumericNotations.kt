package net.derfruhling.serenity.style.notations

import net.derfruhling.serenity.style.Notation
import net.derfruhling.serenity.style.UnparsableNotationException
import net.derfruhling.serenity.style.optRequire
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.roundToLong

sealed class NumberNotation<T> : Notation<T>

object IntNotation : NumberNotation<Int>() {
    override fun test(value: Any): Boolean {
        return value is Int
    }

    override fun asNotationString(value: Int): String {
        return value.toString()
    }

    override fun fromNotationString(value: String): Int {
        try {
            return value.toInt()
        } catch (e: NumberFormatException) {
            throw UnparsableNotationException("Could not parse number: $value", e)
        }
    }
}

val Int.Companion.notation: IntNotation
    get() = IntNotation

object FloatNotation : NumberNotation<Float>() {
    override fun test(value: Any): Boolean {
        return value is Float
    }

    override fun asNotationString(value: Float): String {
        optRequire(value.isFinite()) { "Passed number is not finite" }
        // this is complicated for parity reasons. JS likes to shrink numbers
        // when converting to a string if they are integers, but java and
        // native platforms do not do this. this check ensures the java and
        // native builds omit the decimal point when it shouldn't matter.
        return if(((value % 1).absoluteValue - 0.5f).absoluteValue > 0.4999f && value < Long.MAX_VALUE) {
            value.roundToLong().toString()
        } else {
            value.toString()
        }
    }

    override fun fromNotationString(value: String): Float {
        try {
            return value.toFloat()
        } catch (e: NumberFormatException) {
            throw UnparsableNotationException("Could not parse number: $value", e)
        }
    }
}

val Float.Companion.notation: FloatNotation
    get() = FloatNotation

object DoubleNotation : NumberNotation<Double>() {
    override fun test(value: Any): Boolean {
        return value is Double
    }

    override fun asNotationString(value: Double): String {
        optRequire(value.isFinite()) { "Passed number is not finite" }
        // see FloatNotation for an explanation for this madness.
        return if(((value % 1).absoluteValue - 0.5).absoluteValue > 0.4999 && value < Long.MAX_VALUE) {
            value.roundToLong().toString()
        } else {
            value.toString()
        }
    }

    override fun fromNotationString(value: String): Double {
        try {
            return value.toDouble()
        } catch (e: NumberFormatException) {
            throw UnparsableNotationException("Could not parse number: $value", e)
        }
    }
}

val Double.Companion.notation: DoubleNotation
    get() = DoubleNotation
