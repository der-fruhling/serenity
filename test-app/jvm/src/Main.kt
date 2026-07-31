package net.derfruhling.serenity.testapp

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.derfruhling.serenity.ktor.server.createKtorLogger
import net.derfruhling.serenity.ktor.server.startAwait

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
