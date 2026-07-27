package net.derfruhling.html.ktor.server

import co.touchlab.kermit.Logger
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import platform.posix.SIGHUP
import platform.posix.SIGINT
import platform.posix.SIGKILL
import platform.posix.SIGTERM
import platform.posix.signal
import platform.posix.strsignal

private val closeMutex = MutableStateFlow<Int?>(null)

private fun staticOnInterrupt(signal: Int) {
    closeMutex.value = signal
}

@OptIn(ExperimentalForeignApi::class)
actual fun <E : ApplicationEngine, C : ApplicationEngine.Configuration> EmbeddedServer<E, C>.startAwait(): Unit = runBlocking {
    val log = Logger.withTag("net.derfruhling.html.ktor.server.ComposeHtmlPluginKt")
    start(wait = false)

    signal(SIGINT, staticCFunction(::staticOnInterrupt))
    signal(SIGTERM, staticCFunction(::staticOnInterrupt))
    signal(SIGHUP, staticCFunction(::staticOnInterrupt))

    val signal = closeMutex.filterNotNull().first()
    log.i { "Received ${strsignal(signal)?.toKString()}, stopping server" }
    stopSuspend()
    log.i { "Stopped server" }
}
