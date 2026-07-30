package net.derfruhling.serenity.ktor.server

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.util.AttributeKey
import kotlinx.serialization.KSerializer
import net.derfruhling.serenity.PageHolder
import net.derfruhling.serenity.PageRegistry
import net.derfruhling.serenity.PageTemplate
import net.derfruhling.serenity.TemplateBuilder
import net.derfruhling.serenity.elements.HeadContext
import net.derfruhling.serenity.elements.currentPageLocal
import net.derfruhling.serenity.elements.pageTemplateLocal
import kotlin.reflect.KClass

val pageFunctionName = AttributeKey<String>("pageFunctionName")
private var pageTemplate: PageTemplate? by mutableStateOf(null)

@Deprecated("Use registerServerPages() instead", ReplaceWith("registerServerPages { TODO() }"))
fun Route.register(page: PageHolder) {
    commonRegister(page)
}

private fun Route.commonRegister(page: PageHolder) {
    get(page.path) {
        call.respondCompose {
            CompositionLocalProvider(
                pageTemplateLocal provides pageTemplate
            ) {
                pageTemplate?.BuildPage(mutableStateOf(page))
            }
        }
    }
}

fun Route.registerServerPages(fn: PageRegistry.() -> Unit) {
    (object : PageRegistry() {
        override fun template(fn: @Composable TemplateBuilder.() -> Unit) {
            pageTemplate = PageTemplate(fn)
        }

        override fun <T : PageHolder> register(kClass: KClass<T>, kSerializer: KSerializer<T>, page: T) {
            commonRegister(page)
        }
    }).fn()
}
