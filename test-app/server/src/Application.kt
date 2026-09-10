package net.derfruhling.serenity.testapp

import io.ktor.server.application.*
import io.ktor.server.routing.*
import net.derfruhling.serenity.server.ktor.Serenity
import net.derfruhling.serenity.server.ktor.registerServerPages
import net.derfruhling.serenity.server.ktor.serveStatic

fun Application.configure() {
    install(Serenity)

    routing {
        serveStatic()

        registerServerPages { registerPages() }
    }
}
