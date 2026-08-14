package net.derfruhling.serenity.style

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.style.enums.DisplayMode
import net.derfruhling.serenity.style.notations.LengthPercentage

@Composable
fun Style.display(displayMode: DisplayMode) =
    Rule("display", DisplayMode.notation, displayMode)

@Composable
fun Style.width(value: LengthPercentage) =
    Rule("width", LengthPercentage.notation, value)

@Composable
fun Style.height(value: LengthPercentage) =
    Rule("height", LengthPercentage.notation, value)
