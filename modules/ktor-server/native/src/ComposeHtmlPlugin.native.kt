package net.derfruhling.serenity.ktor.server

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.engine.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import platform.posix.*

private val closeMutex = MutableStateFlow<Int?>(null)

private fun staticOnInterrupt(signal: Int) {
    closeMutex.value = signal
}

@OptIn(ExperimentalForeignApi::class)
actual fun <E : ApplicationEngine, C : ApplicationEngine.Configuration> EmbeddedServer<E, C>.startAwait(): Unit =
    runBlocking {
        val log = KotlinLogging.logger {}
        start(wait = false)

        signal(SIGINT, staticCFunction(::staticOnInterrupt))
        signal(SIGTERM, staticCFunction(::staticOnInterrupt))
        signal(SIGHUP, staticCFunction(::staticOnInterrupt))

        val signal = closeMutex.filterNotNull().first()
        log.info { "Received ${strsignal(signal)?.toKString()}, stopping server" }
        stopSuspend()
        log.info { "Stopped server" }
    }

actual fun ComposeHtmlConfig.readManifest(): String {
    return SystemFileSystem.source(manifestPath).use {
        it.buffered().readString()
    }
}
