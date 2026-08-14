package net.derfruhling.serenity.style.enums

import net.derfruhling.serenity.style.notations.EnumNotation
import net.derfruhling.serenity.style.notations.enumNotation

enum class DisplayMode {
    BLOCK,
    INLINE,
    INLINE_BLOCK,
    FLEX;

    companion object {
        val notation: EnumNotation<DisplayMode> by enumNotation()
    }
}
