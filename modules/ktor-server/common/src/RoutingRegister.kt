package net.derfruhling.serenity.server.ktor

import androidx.compose.runtime.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.serialization.KSerializer
import net.derfruhling.serenity.*
import net.derfruhling.serenity.elements.pageTemplateLocal
import net.derfruhling.serenity.modularity.extension.PageExtensionController
import net.derfruhling.serenity.modularity.extension.pageExtensionControllerLocal
import net.derfruhling.serenity.serial.SerialRegistry
import kotlin.reflect.KClass

val pageFunctionName = AttributeKey<String>("pageFunctionName")
private var pageTemplate: PageTemplate? by mutableStateOf(null)

private fun Route.commonRegister(page: PageHolderFactory<ApplicationCall, *>) {
    get(page.path) {
        call.respondCompose {
            val page = remember { page.create(call) }
            val extensionController =
                remember(page) { PageExtensionController().also { it.setPage(page) } }
            CompositionLocalProvider(
                pageTemplateLocal provides pageTemplate,
                pageExtensionControllerLocal provides extensionController
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
