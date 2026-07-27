package net.derfruhling.html.testapp

import net.derfruhling.html.onHtmlContextStart

fun main() {
    onHtmlContextStart {
        it.enableDebugMode = true
    }
}
