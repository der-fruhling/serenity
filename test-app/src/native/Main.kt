package net.derfruhling.html.testapp

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import io.ktor.server.application.ServerConfig
import io.ktor.server.application.serverConfig
import io.ktor.server.cio.*
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.configLoaders
import io.ktor.server.engine.*
import kotlinx.io.files.Path
import net.derfruhling.html.AnsiColorCodeMessageFormatter
import net.derfruhling.html.LogWriters
import net.derfruhling.html.NeatMessageFormatter
import net.derfruhling.html.ktor.server.createKtorLogger

fun main() {
    LogWriters.createFileLogWriter(NeatMessageFormatter(), Path("server.log")).use { fileLog ->
        Logger.setMinSeverity(Severity.Verbose)
        Logger.setLogWriters(LogWriters.createStandardLogWriter(AnsiColorCodeMessageFormatter()), fileLog)

        embeddedServer(CIO, applicationEnvironment {
            log = createKtorLogger()
        }, {
            connector {
                host = "127.0.0.1"
                port = 8080
            }
        }) { configure() }.start(wait = true)
    }
}