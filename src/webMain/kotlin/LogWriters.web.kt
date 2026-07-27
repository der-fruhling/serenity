package net.derfruhling.html

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Message
import co.touchlab.kermit.MessageStringFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Tag
import web.console.console

actual object LogWriters {
    actual fun createStandardLogWriter(fmt: MessageStringFormatter): LogWriter {
        return object : LogWriter() {
            override fun log(
                severity: Severity,
                message: String,
                tag: String,
                throwable: Throwable?
            ) {
                val message = fmt.formatMessage(severity, Tag(tag), Message(message))
                when(severity) {
                    Severity.Verbose, Severity.Debug ->
                        console.debug(message)
                    Severity.Info -> console.info(message)
                    Severity.Warn -> console.warn(message)
                    Severity.Error -> console.error(message)

                    Severity.Assert ->
                        @OptIn(ExperimentalWasmJsInterop::class)
                        console.assert(false, message.toJsString())
                }
            }
        }
    }
}