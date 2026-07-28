package net.derfruhling.serenity.testapp

import io.ktor.server.application.*
import io.ktor.server.routing.*
import net.derfruhling.serenity.ktor.server.ComposeHtml
import net.derfruhling.serenity.ktor.server.registerServerPages
import net.derfruhling.serenity.ktor.server.serveStatic

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
