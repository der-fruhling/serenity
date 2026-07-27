package net.derfruhling.html.testapp

import net.derfruhling.html.SerialRegistry
import net.derfruhling.html.onHtmlContextStart
import net.derfruhling.html.registerClientPages

fun main() {
    SerialRegistry.registerClientPages { registerPages() }

    onHtmlContextStart { it.enableDebugMode = true }
}
