package net.derfruhling.serenity.style.notations

import androidx.compose.runtime.Immutable
import net.derfruhling.serenity.style.Notation
import net.derfruhling.serenity.style.optRequire
import kotlin.jvm.JvmInline

@JvmInline
@Immutable
value class Percentage(val actualValue: Float) : LengthPercentage {
    override val isRelative: Boolean
        get() = true

    companion object {
        val notation: PercentageNotation
            get() = PercentageNotation
    }
}

val Int.percent: Percentage
    get() = Percentage(toFloat())
val Float.percent: Percentage
    get() = Percentage(this)

object PercentageNotation : Notation<Percentage> {
    override fun test(value: Any): Boolean {
        return value is Percentage
    }

    override fun asNotationString(value: Percentage): String {
        return FloatNotation.asNotationString(value.actualValue) + '%'
    }

    override fun fromNotationString(value: String): Percentage {
        optRequire(value.endsWith('%')) { "Not a percentage" }
        return Percentage(FloatNotation.fromNotationString(value.substring(0, value.length - 1)))
    }
}
