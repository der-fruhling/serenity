package net.derfruhling.html.testapp

import io.ktor.server.engine.applicationEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import net.derfruhling.html.ktor.server.createKtorLogger
import net.derfruhling.html.ktor.server.startAwait

fun main() {
    embeddedServer(Netty, applicationEnvironment {
        log = createKtorLogger()
    }, {
        connector {
            host = "127.0.0.1"
            port = 8080
        }
    }) { configure() }.startAwait()
}
