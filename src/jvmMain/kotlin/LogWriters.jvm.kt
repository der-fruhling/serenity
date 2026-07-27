package net.derfruhling.html

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.MessageStringFormatter
import co.touchlab.kermit.Severity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

actual object LogWriters {
    actual fun createStandardLogWriter(fmt: MessageStringFormatter): LogWriter {
        return object : LogWriter() {
            val loggerFactory = LoggerFactory.getILoggerFactory()

            private val loggerCache = mutableMapOf<String, Logger>()

            override fun log(
                severity: Severity,
                message: String,
                tag: String,
                throwable: Throwable?
            ) {
                loggerCache.computeIfAbsent(tag) { loggerFactory.getLogger(it) }
                    .atLevel(when(severity) {
                        Severity.Verbose -> Level.TRACE
                        Severity.Debug -> Level.DEBUG
                        Severity.Info -> Level.INFO
                        Severity.Warn -> Level.WARN
                        Severity.Error -> Level.ERROR
                        Severity.Assert -> Level.ERROR
                    }).let { builder ->
                        if(throwable != null) {
                            builder.log(message, throwable)
                        } else {
                            builder.log(message)
                        }
                    }
            }
        }
    }
}
