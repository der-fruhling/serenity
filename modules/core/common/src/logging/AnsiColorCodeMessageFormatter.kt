package net.derfruhling.serenity.logging

import io.github.oshai.kotlinlogging.Level
import kotlin.time.Instant

class AnsiColorCodeMessageFormatter(
    val traceColor: AnsiColorState = AnsiColorState(AnsiColorCode.GRAY),
    val debugColor: AnsiColorState = AnsiColorState(AnsiColorCode.PURPLE),
    val infoColor: AnsiColorState = AnsiColorState(AnsiColorCode.BRIGHT_GREEN),
    val warnColor: AnsiColorState = AnsiColorState(AnsiColorCode.BRIGHT_YELLOW, underline = true),
    val errorColor: AnsiColorState = AnsiColorState(AnsiColorCode.BRIGHT_RED, underline = true, bold = true),
    val assertColor: AnsiColorState = AnsiColorState(AnsiColorCode.BRIGHT_WHITE, bg = AnsiColorCode.RED, underline = true),
    val tagColor: AnsiColorState = AnsiColorState(AnsiColorCode.BRIGHT_CYAN),
    val timeColor: AnsiColorState = AnsiColorState(AnsiColorCode.BRIGHT_PURPLE)
) : NeatMessageFormatter() {
    override fun formatLevel(level: Level): String {
        return when(level) {
            Level.TRACE -> traceColor
            Level.DEBUG -> debugColor
            Level.INFO -> infoColor
            Level.WARN -> warnColor
            Level.ERROR -> errorColor
            else -> AnsiColorState()
        }.value + super.formatLevel(level) + AnsiColorState.RESET
    }

    override fun formatName(name: String): String {
        return tagColor.value + super.formatName(name) + AnsiColorState.RESET
    }

    override fun formatTime(instant: Instant): String {
        return timeColor.value + super.formatTime(instant) + AnsiColorState.RESET
    }
}
