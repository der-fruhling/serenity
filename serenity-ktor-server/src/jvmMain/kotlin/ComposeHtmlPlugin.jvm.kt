package net.derfruhling.serenity.ktor.server

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sun.misc.Signal

actual fun <E : ApplicationEngine, C : ApplicationEngine.Configuration> EmbeddedServer<E, C>.startAwait(): Unit =
    runBlocking {
        val log = KotlinLogging.logger {}

        for(name in arrayOf("INT", "TERM", "HUP")) {
            Signal.handle(Signal(name)) {
                launch {
                    log.info { "Received ${it.name}, stopping" }
                    stopSuspend()
                    log.info { "Stopped server" }
                }
            }
        }

        startSuspend(wait = true)
    }
