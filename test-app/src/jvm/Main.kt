package net.derfruhling.html.testapp

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import io.ktor.server.engine.applicationEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import net.derfruhling.html.AnsiColorCodeMessageFormatter
import net.derfruhling.html.LogWriters
import net.derfruhling.html.ktor.server.createKtorLogger

fun main() {
    Logger.setMinSeverity(Severity.Verbose)
    Logger.setLogWriters(LogWriters.createStandardLogWriter(AnsiColorCodeMessageFormatter()))

    embeddedServer(Netty, applicationEnvironment {
        log = createKtorLogger()
    }, {
        connector {
            host = "127.0.0.1"
            port = 8080
        }
    }) { configure() }.start(wait = true)
}
