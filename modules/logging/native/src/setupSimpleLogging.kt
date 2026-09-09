package net.derfruhling.serenity.logging

import io.github.oshai.kotlinlogging.*
import kotlinx.io.files.Path

inline fun setupSimpleLogging(
    logLevel: Level = Level.DEBUG,
    filePath: Path = Path("server.log"),
    fileFormatter: Formatter = NeatMessageFormatter(),
    consoleFormatter: Formatter = AnsiColorCodeMessageFormatter(),
    fn: () -> Unit
) {
    LogWriters.createFileLogWriter(fileFormatter, filePath).use { fileLog ->
        KotlinLoggingConfiguration.loggerFactory = DirectLoggerFactory
        KotlinLoggingConfiguration.direct.logLevel = logLevel
        KotlinLoggingConfiguration.direct.appender = SimpleAppenderImpl(fileLog, consoleFormatter)

        fn()
    }
}

@PublishedApi
internal class SimpleAppenderImpl(val fileLog: Appender, consoleFormatter: Formatter) : Appender {
    val consoleLog = LogWriters.createStandardLogWriter(consoleFormatter)

    override fun log(loggingEvent: KLoggingEvent) {
        fileLog.log(loggingEvent)
        consoleLog.log(loggingEvent)
    }
}
