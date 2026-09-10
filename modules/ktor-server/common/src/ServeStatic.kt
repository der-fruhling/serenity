package net.derfruhling.serenity.server.ktor

import io.ktor.server.routing.*

expect fun Route.serveStatic(remotePath: String = "/_/")
