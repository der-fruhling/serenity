package net.derfruhling.html.ktor.server

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import io.ktor.server.application.Application
import io.ktor.util.logging.LogLevel
import io.ktor.util.logging.Logger as KtorLogger

var ktorLogger: Logger = Logger.withTag(Application::class.qualifiedName!!)

actual fun createKtorLogger(): KtorLogger {
    return object : KtorLogger {
        override val level: LogLevel
            get() = when (ktorLogger.mutableConfig.minSeverity) {
                Severity.Verbose -> LogLevel.TRACE
                Severity.Debug -> LogLevel.DEBUG
                Severity.Info -> LogLevel.INFO
                Severity.Warn -> LogLevel.WARN
                Severity.Error -> LogLevel.ERROR
                Severity.Assert -> LogLevel.ERROR
            }

        override fun error(message: String) {
            ktorLogger.e(message)
        }

        override fun error(message: String, cause: Throwable) {
            ktorLogger.e(message, cause)
        }

        override fun warn(message: String) {
            ktorLogger.w(message)
        }

        override fun warn(message: String, cause: Throwable) {
            ktorLogger.w(message, cause)
        }

        override fun info(message: String) {
            ktorLogger.i(message)
        }

        override fun info(message: String, cause: Throwable) {
            ktorLogger.i(message, cause)
        }

        override fun debug(message: String) {
            ktorLogger.d(message)
        }

        override fun debug(message: String, cause: Throwable) {
            ktorLogger.d(message, cause)
        }

        override fun trace(message: String) {
            ktorLogger.v(message)
        }

        override fun trace(message: String, cause: Throwable) {
            ktorLogger.v(message, cause)
        }
    }
}
