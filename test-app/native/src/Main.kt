package net.derfruhling.html.testapp

import io.github.oshai.kotlinlogging.Appender
import io.github.oshai.kotlinlogging.DirectLoggerFactory
import io.github.oshai.kotlinlogging.KLoggingEvent
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.io.files.Path
import net.derfruhling.html.AnsiColorCodeMessageFormatter
import net.derfruhling.html.LogWriters
import net.derfruhling.html.NeatMessageFormatter
import net.derfruhling.html.ktor.server.createKtorLogger
import net.derfruhling.html.ktor.server.startAwait

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