package net.derfruhling.serenity.ktor.server

import androidx.compose.runtime.*
import io.ktor.server.application.ApplicationCall
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.serialization.KSerializer
import net.derfruhling.serenity.PageHolder
import net.derfruhling.serenity.PageHolderFactory
import net.derfruhling.serenity.PageRegistry
import net.derfruhling.serenity.PageTemplate
import net.derfruhling.serenity.SerialRegistry
import net.derfruhling.serenity.TemplateBuilder
import net.derfruhling.serenity.elements.pageTemplateLocal
import kotlin.reflect.KClass

val pageFunctionName = AttributeKey<String>("pageFunctionName")
private var pageTemplate: PageTemplate? by mutableStateOf(null)

private fun Route.commonRegister(page: PageHolderFactory<ApplicationCall, *>) {
    get(page.path) {
        call.respondCompose {
            val page = remember { page.create(call) }
            CompositionLocalProvider(
                pageTemplateLocal provides pageTemplate
            ) {
                pageTemplate?.BuildPage(mutableStateOf(page))
            }
        }
    }
}

fun Route.registerServerPages(fn: PageRegistry<ApplicationCall>.() -> Unit) {
    (object : PageRegistry<ApplicationCall>() {
        override fun template(fn: @Composable TemplateBuilder.() -> Unit) {
            pageTemplate = PageTemplate(fn)
        }

        override fun <R : PageHolder<R>, T : PageHolderFactory<ApplicationCall, R>> register(
            kClass: KClass<R>,
            kSerializer: KSerializer<R>,
            page: T
        ) {
            SerialRegistry.registerPage(kClass, kSerializer)
            commonRegister(page)
        }
    }).fn()
}
