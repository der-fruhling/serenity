package net.derfruhling.html.ktor.server

import androidx.compose.runtime.withCompositionLocal
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.util.AttributeKey
import kotlinx.serialization.KSerializer
import net.derfruhling.html.PageHolder
import net.derfruhling.html.PageRegistry
import kotlin.reflect.KClass

val pageFunctionName = AttributeKey<String>("pageFunctionName")

@Deprecated("Use registerServerPages() instead", ReplaceWith("registerServerPages { TODO() }"))
fun Route.register(page: PageHolder) {
    commonRegister(page)
}

private fun Route.commonRegister(page: PageHolder) {
    get(page.path) {
        call.respondCompose {
            withCompositionLocal(applicationCallLocal provides call) {
                page.Main()
            }
        }
    }
}

fun Route.registerServerPages(fn: PageRegistry.() -> Unit) {
    (object : PageRegistry {
        override fun <T : PageHolder> register(kClass: KClass<T>, kSerializer: KSerializer<T>, page: T) {
            commonRegister(page)
        }
    }).fn()
}
