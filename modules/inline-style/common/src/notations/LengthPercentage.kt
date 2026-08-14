package net.derfruhling.serenity.style.notations

import androidx.compose.runtime.Immutable
import net.derfruhling.serenity.style.Notation

@Immutable
sealed interface LengthPercentage {
    val isRelative: Boolean

    companion object {
        val notation = Notation.of(
            Percentage.notation,
            Length.notation
        )
    }
}

typealias Position = LengthPercentage
