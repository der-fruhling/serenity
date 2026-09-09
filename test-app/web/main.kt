package net.derfruhling.serenity.testapp

import net.derfruhling.serenity.SerialRegistry
import net.derfruhling.serenity.logging.setupConsoleLogging
import net.derfruhling.serenity.onHtmlContextStart
import net.derfruhling.serenity.registerClientPages

fun main() {
    setupConsoleLogging()

    commonMain()
    SerialRegistry.registerClientPages { registerPages() }

    onHtmlContextStart { it.enableDebugMode = true }
}
