package net.derfruhling.serenity.testapp

import io.ktor.server.cio.*
import io.ktor.server.engine.*
import net.derfruhling.serenity.logging.setupSimpleLogging
import net.derfruhling.serenity.server.ktor.createKtorLogger
import net.derfruhling.serenity.server.ktor.startAwait

fun main() = setupSimpleLogging {
    commonMain()
    embeddedServer(CIO, applicationEnvironment {
        log = createKtorLogger()
    }, {
        connector {
            host = "127.0.0.1"
            port = 8080
        }
    }) { configure() }.startAwait()
}
