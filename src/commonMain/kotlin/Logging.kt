package net.derfruhling.html

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Message
import co.touchlab.kermit.MessageStringFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Tag
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char
import kotlinx.datetime.offsetAt
import kotlin.time.Clock
import kotlin.time.Instant

expect object LogWriters {
    fun createStandardLogWriter(fmt: MessageStringFormatter): LogWriter
}

open class NeatMessageFormatter : MessageStringFormatter {
    override fun formatSeverity(severity: Severity): String = when(severity) {
        Severity.Verbose -> "VERBOSE"
        Severity.Debug -> "DEBUG"
        Severity.Info -> "INFO"
        Severity.Warn -> "WARN"
        Severity.Error -> "ERROR"
        Severity.Assert -> "ASSERT"
    }

    override fun formatTag(tag: Tag): String = tag.tag

    open val dateTimeFormatter: DateTimeFormat<DateTimeComponents> =
        DateTimeComponents.Format {
            date(LocalDate.Formats.ISO)
            char(' ')

            hour()
            char(':')
            minute()
            char(':')
            second()
            char('.')
            secondFraction(8)

            char(' ')
            offset(UtcOffset.Formats.ISO)
        }

    open fun formatTime(instant: Instant): String =
        instant.format(dateTimeFormatter, TimeZone.currentSystemDefault().offsetAt(instant))

    override fun formatMessage(
        severity: Severity?,
        tag: Tag?,
        message: Message
    ): String {
        val instant = Clock.System.now()
        val time = formatTime(instant)
        return "$time <${formatTag(tag!!)}> ${formatSeverity(severity!!)}: ${message.message}"
    }
}

enum class AnsiColorCode(val fg: String, val bg: String) {
    BLACK("30", "40"),
    RED("31", "41"),
    GREEN("32", "42"),
    YELLOW("33", "43"),
    BLUE("34", "44"),
    PURPLE("35", "45"),
    CYAN("36", "46"),
    LIGHT_GRAY("37", "47"),
    GRAY("90", "100"),
    BRIGHT_RED("91", "101"),
    BRIGHT_GREEN("92", "102"),
    BRIGHT_YELLOW("93", "103"),
    BRIGHT_BLUE("94", "104"),
    BRIGHT_PURPLE("95", "105"),
    BRIGHT_CYAN("96", "106"),
    BRIGHT_WHITE("97", "107"),
}

data class AnsiColorState(
    val fg: AnsiColorCode? = null,
    val bold: Boolean = false,
    val underline: Boolean = false,
    val bg: AnsiColorCode? = null
) {
    val value: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        buildString {
            append("\u001b[")
            append(buildList {
                if(fg != null) add(fg.fg)
                if(bold) add(BOLD)
                if(underline) add(UNDERLINE)
                if(bg != null) add(bg.bg)
            }.joinToString(";"))
            append('m')
        }
    }

    companion object {
        private const val BOLD = "1"
        private const val UNDERLINE = "4"

        const val RESET = "\u001b[0m"
    }
}

class AnsiColorCodeMessageFormatter(
    val verboseColor: AnsiColorState = AnsiColorState(AnsiColorCode.GRAY),
    val debugColor: AnsiColorState = AnsiColorState(AnsiColorCode.PURPLE),
    val infoColor: AnsiColorState = AnsiColorState(AnsiColorCode.BRIGHT_GREEN),
    val warnColor: AnsiColorState = AnsiColorState(AnsiColorCode.BRIGHT_YELLOW, underline = true),
    val errorColor: AnsiColorState = AnsiColorState(AnsiColorCode.BRIGHT_RED, underline = true, bold = true),
    val assertColor: AnsiColorState = AnsiColorState(AnsiColorCode.BRIGHT_WHITE, bg = AnsiColorCode.RED, underline = true),
    val tagColor: AnsiColorState = AnsiColorState(AnsiColorCode.BRIGHT_CYAN),
    val timeColor: AnsiColorState = AnsiColorState(AnsiColorCode.BRIGHT_PURPLE)
) : NeatMessageFormatter() {
    override fun formatSeverity(severity: Severity): String {
        return when(severity) {
            Severity.Verbose -> verboseColor
            Severity.Debug -> debugColor
            Severity.Info -> infoColor
            Severity.Warn -> warnColor
            Severity.Error -> errorColor
            Severity.Assert -> assertColor
        }.value + super.formatSeverity(severity) + AnsiColorState.RESET
    }

    override fun formatTag(tag: Tag): String {
        return tagColor.value + super.formatTag(tag) + AnsiColorState.RESET
    }

    override fun formatTime(instant: Instant): String {
        return timeColor.value + super.formatTime(instant) + AnsiColorState.RESET
    }
}

expect inline fun <reified T> Logger.of(): Logger
