package net.derfruhling.serenity.logging

import io.github.oshai.kotlinlogging.DirectLoggerFactory
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level

actual inline fun setupLogging(fn: () -> Unit) {
    setupConsoleLogging()
    fn()
}

fun setupConsoleLogging(logLevel: Level = Level.DEBUG) {
    KotlinLoggingConfiguration.loggerFactory = DirectLoggerFactory
    KotlinLoggingConfiguration.direct.logLevel = logLevel
    KotlinLoggingConfiguration.direct.appender = ConsoleAppender()
}
