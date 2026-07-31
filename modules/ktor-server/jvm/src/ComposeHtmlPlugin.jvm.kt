package net.derfruhling.serenity.ktor.server

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.engine.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import sun.misc.Signal

actual fun <E : ApplicationEngine, C : ApplicationEngine.Configuration> EmbeddedServer<E, C>.startAwait(): Unit =
    runBlocking {
        val log = KotlinLogging.logger {}

        for (name in arrayOf("INT", "TERM", "HUP")) {
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

actual fun ComposeHtmlConfig.readManifest(): String {
    return if (manifestAlwaysInFileSystem) {
        SystemFileSystem.source(manifestPath).use {
            it.buffered().readString()
        }
    } else {
        ClassLoader.getSystemClassLoader().getResource(manifestPath.toString())?.readText()
            ?: throw IllegalStateException("Application manifest not found at root of classpath")
    }
}