package net.derfruhling.html.ktor.server

import androidx.compose.runtime.withCompositionLocal
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.util.AttributeKey
import net.derfruhling.html.PageHolder

val pageFunctionName = AttributeKey<String>("pageFunctionName")

fun Route.register(page: PageHolder) {
    get(page.path) {
        call.respondCompose {
            withCompositionLocal(applicationCallLocal provides call) {
                page.Main()
            }
        }
    }
}
