package net.derfruhling.html.ktor.server

import io.ktor.server.http.content.staticResources
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.path

actual fun Route.serveStatic(remotePath: String) {
    application.attributes[staticFilePath] = staticResources(remotePath, "_static", null).path
}
