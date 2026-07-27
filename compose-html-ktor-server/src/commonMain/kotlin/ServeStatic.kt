package net.derfruhling.html.ktor.server

import io.ktor.server.routing.*

expect fun Route.serveStatic(remotePath: String = "/_/")
