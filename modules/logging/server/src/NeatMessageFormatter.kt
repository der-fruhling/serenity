package net.derfruhling.serenity.logging

import io.github.oshai.kotlinlogging.Formatter
import io.github.oshai.kotlinlogging.KLoggingEvent
import io.github.oshai.kotlinlogging.Level
import kotlinx.datetime.*
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char
import kotlin.time.Instant

open class NeatMessageFormatter : Formatter {
    open fun formatLevel(level: Level): String = level.name

    open fun formatName(name: String): String = name

    override fun formatMessage(loggingEvent: KLoggingEvent): String {
        val time = formatTime(Instant.fromEpochMilliseconds(loggingEvent.timestamp))
        val name = formatName(loggingEvent.loggerName)
        val level = formatLevel(loggingEvent.level)
        return "$time <$name> $level: ${loggingEvent.message}" + loggingEvent.cause?.let {
            '\n' + formatException(
                it.stackTraceToString()
            )
        }
    }

    open fun formatException(stackTrace: String) = stackTrace

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
}