package net.derfruhling.html.testapp

import io.ktor.server.application.*
import io.ktor.server.routing.*
import net.derfruhling.html.ktor.server.ComposeHtml
import net.derfruhling.html.ktor.server.registerServerPages
import net.derfruhling.html.ktor.server.serveStatic

fun Application.configure() {
    install(ComposeHtml) {
        useScript("/_/js/page.js")
        useEntrypoint("test-app")
    }

    routing {
        serveStatic()

        registerServerPages { registerPages() }
    }
}
