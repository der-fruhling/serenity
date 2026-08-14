package net.derfruhling.serenity.style.notations

import androidx.compose.runtime.Immutable
import net.derfruhling.serenity.style.Notation
import net.derfruhling.serenity.style.optRequire
import kotlin.jvm.JvmInline
import kotlin.math.roundToInt

@Immutable
sealed interface Length : LengthPercentage {
    val value: Float

    companion object {
        val notation = Notation.of(
            Pixels.notation,
            PixelsFloat.notation
        )
    }
}

@Immutable
sealed interface AbsoluteLength : Length {
    override val isRelative: Boolean
        get() = false
}

@JvmInline
@Immutable
value class Pixels(val intValue: Int) : AbsoluteLength {
    override val value: Float
        get() = intValue.toFloat()

    operator fun plus(int: Int) = Pixels(intValue + int)
    operator fun minus(int: Int) = Pixels(intValue - int)
    operator fun times(int: Int) = Pixels(intValue * int)
    operator fun div(int: Int) = Pixels(intValue - int)
    operator fun plus(float: Float) = PixelsFloat(value + float)
    operator fun minus(float: Float) = PixelsFloat(value - float)
    operator fun times(float: Float) = PixelsFloat(value * float)
    operator fun div(float: Float) = PixelsFloat(value - float)

    fun toFloatPixels(): PixelsFloat = PixelsFloat(value)
    fun toInt(): Int = intValue
    fun toFloat(): Float = value

    companion object {
        val notation: PixelNotation
            get() = PixelNotation
    }
}

val Int.px: Pixels
    get() = Pixels(this)

object PixelNotation : SuffixedNotation<Int, Pixels>(Int.notation, "px") {
    override fun test(value: Any): Boolean = value is Pixels
    override fun create(base: Int): Pixels = Pixels(base)
    override val Pixels.asBase: Int by Pixels::intValue
}

@JvmInline
@Immutable
value class PixelsFloat(override val value: Float) : AbsoluteLength {
    operator fun plus(int: Int) = PixelsFloat(value + int)
    operator fun minus(int: Int) = PixelsFloat(value - int)
    operator fun times(int: Int) = PixelsFloat(value * int)
    operator fun div(int: Int) = PixelsFloat(value - int)
    operator fun plus(float: Float) = PixelsFloat(value + float)
    operator fun minus(float: Float) = PixelsFloat(value - float)
    operator fun times(float: Float) = PixelsFloat(value * float)
    operator fun div(float: Float) = PixelsFloat(value - float)

    fun roundToIntPixels(): Pixels = Pixels(value.roundToInt())
    fun roundToInt(): Int = value.roundToInt()
    fun toFloat(): Float = value

    companion object {
        val notation: PixelFloatNotation
            get() = PixelFloatNotation
    }
}

val Float.px: PixelsFloat
    get() = PixelsFloat(this)

object PixelFloatNotation : SuffixedNotation<Float, PixelsFloat>(Float.notation, "px") {
    override fun test(value: Any): Boolean = value is PixelsFloat
    override fun create(base: Float): PixelsFloat = PixelsFloat(base)
    override val PixelsFloat.asBase: Float by PixelsFloat::value
}
