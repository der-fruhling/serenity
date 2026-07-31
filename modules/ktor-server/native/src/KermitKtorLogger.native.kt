package net.derfruhling.serenity.ktor.server

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level
import io.ktor.server.application.*
import io.ktor.util.logging.*
import io.ktor.util.logging.Logger as KtorLogger

var ktorLogger: KLogger = KotlinLogging.logger(Application::class.qualifiedName!!)

actual fun createKtorLogger(): KtorLogger {
    return object : KtorLogger {
        override val level: LogLevel
            get() = when (KotlinLoggingConfiguration.direct.logLevel) {
                Level.TRACE -> LogLevel.TRACE
                Level.DEBUG -> LogLevel.DEBUG
                Level.INFO -> LogLevel.INFO
                Level.WARN -> LogLevel.WARN
                Level.ERROR -> LogLevel.ERROR
                else -> kotlin.error("what")
            }

        override fun error(message: String) {
            ktorLogger.error { message }
        }

        override fun error(message: String, cause: Throwable) {
            ktorLogger.error(cause) { message }
        }

        override fun warn(message: String) {
            ktorLogger.warn { message }
        }

        override fun warn(message: String, cause: Throwable) {
            ktorLogger.warn(cause) { message }
        }

        override fun info(message: String) {
            ktorLogger.info { message }
        }

        override fun info(message: String, cause: Throwable) {
            ktorLogger.info(cause) { message }
        }

        override fun debug(message: String) {
            ktorLogger.debug { message }
        }

        override fun debug(message: String, cause: Throwable) {
            ktorLogger.debug(cause) { message }
        }

        override fun trace(message: String) {
            ktorLogger.trace { message }
        }

        override fun trace(message: String, cause: Throwable) {
            ktorLogger.trace(cause) { message }
        }
    }
}
