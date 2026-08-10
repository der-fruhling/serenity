package net.derfruhling.serenity.testapp

import io.github.oshai.kotlinlogging.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.io.files.Path
import net.derfruhling.serenity.LogWriters
import net.derfruhling.serenity.ktor.server.createKtorLogger
import net.derfruhling.serenity.ktor.server.startAwait
import net.derfruhling.serenity.logging.AnsiColorCodeMessageFormatter
import net.derfruhling.serenity.logging.NeatMessageFormatter

fun main() {
    LogWriters.createFileLogWriter(NeatMessageFormatter(), Path("server.log")).use { fileLog ->
        KotlinLoggingConfiguration.loggerFactory = DirectLoggerFactory
        KotlinLoggingConfiguration.direct.logLevel = Level.TRACE
        KotlinLoggingConfiguration.direct.appender = object : Appender {
            val consoleLog = LogWriters.createStandardLogWriter(AnsiColorCodeMessageFormatter())

            override fun log(loggingEvent: KLoggingEvent) {
                fileLog.log(loggingEvent)
                consoleLog.log(loggingEvent)
            }
        }

        embeddedServer(CIO, applicationEnvironment {
            log = createKtorLogger()
        }, {
                           connector {
                               host = "127.0.0.1"
                               port = 8080
                           }
                       }) { configure() }.startAwait()
    }
}