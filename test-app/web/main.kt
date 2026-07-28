package net.derfruhling.serenity.testapp

import net.derfruhling.serenity.SerialRegistry
import net.derfruhling.serenity.onHtmlContextStart
import net.derfruhling.serenity.registerClientPages

fun main() {
    SerialRegistry.registerClientPages { registerPages() }

    onHtmlContextStart { it.enableDebugMode = true }
}
