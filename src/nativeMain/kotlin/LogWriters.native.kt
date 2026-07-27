package net.derfruhling.html

import co.touchlab.kermit.*
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString

actual object LogWriters {
    actual fun createStandardLogWriter(fmt: MessageStringFormatter): LogWriter {
        return object : LogWriter() {
            val sync = SynchronizedObject()

            override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
                val message = fmt.formatMessage(severity, Tag(tag), Message(message))
                synchronized(sync) {
                    println(message)
                }
            }
        }
    }

    abstract class ClosableLogWriter : LogWriter(), AutoCloseable

    fun createFileLogWriter(fmt: MessageStringFormatter, path: Path, append: Boolean = true): ClosableLogWriter {
        return object : ClosableLogWriter() {
            val sync = SynchronizedObject()
            val file = SystemFileSystem.sink(path, append).buffered()

            override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
                val message = fmt.formatMessage(severity, Tag(tag), Message(message)) + '\n'
                synchronized(sync) {
                    file.writeString(message)
                }
            }

            override fun close() {
                file.close()
            }
        }
    }
}