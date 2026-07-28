package net.derfruhling.serenity.ktor.server

import io.ktor.server.routing.*

expect fun Route.serveStatic(remotePath: String = "/_/")
