package net.derfruhling.serenity

import io.github.oshai.kotlinlogging.Appender
import io.github.oshai.kotlinlogging.Formatter
import io.github.oshai.kotlinlogging.KLoggingEvent
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString

object LogWriters {
    fun createStandardLogWriter(fmt: Formatter): Appender {
        return object : Appender {
            val sync = SynchronizedObject()

            override fun log(loggingEvent: KLoggingEvent) {
                val message = fmt.formatMessage(loggingEvent)
                synchronized(sync) {
                    println(message)
                }
            }
        }
    }

    abstract class ClosableAppender : Appender, AutoCloseable

    fun createFileLogWriter(fmt: Formatter, path: Path, append: Boolean = true): ClosableAppender {
        return object : ClosableAppender() {
            val sync = SynchronizedObject()
            val file = SystemFileSystem.sink(path, append).buffered()

            override fun log(loggingEvent: KLoggingEvent) {
                val message = fmt.formatMessage(loggingEvent) + '\n'
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