package net.derfruhling.html.ktor.server

import io.ktor.server.application.Application
import org.slf4j.LoggerFactory
import io.ktor.util.logging.Logger as KtorLogger

actual fun createKtorLogger(): KtorLogger {
    return LoggerFactory.getLogger(Application::class.java)
}
