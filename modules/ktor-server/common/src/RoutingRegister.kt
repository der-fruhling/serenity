package net.derfruhling.serenity.server.ktor

import androidx.compose.runtime.*
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import net.derfruhling.serenity.*
import net.derfruhling.serenity.elements.pageTemplateLocal
import net.derfruhling.serenity.modularity.extension.PageExtensionController
import net.derfruhling.serenity.modularity.extension.pageExtensionControllerLocal
import net.derfruhling.serenity.serial.SerialRegistry
import kotlin.reflect.KClass

val pageFunctionName = AttributeKey<String>("pageFunctionName")
private var pageTemplate: PageTemplate? by mutableStateOf(null)
private val LOGGER: KLogger = KotlinLogging.logger {}

private class PageInitReturnedErrorStatusException(val httpStatus: HttpStatus, val detail: StatusDetail) : RuntimeException()

private class PageContextImpl(val call: ApplicationCall, val scope: CoroutineScope, val job: Job) : PageContext() {
    val pageServerInit = object : PageServerInit() {
        override suspend fun respond(
            status: HttpStatus,
            detail: StatusDetail
        ): Nothing {
            throw PageInitReturnedErrorStatusException(status, detail)
        }
    }

    val inits = mutableListOf<Job>()

    override fun getParameter(name: String): String? {
        return call.parameters[name]
    }

    override fun registerInitFun(fn: suspend PageInit.() -> Unit) {
        inits += scope.launch { pageServerInit.fn() }
    }
}

private fun Route.commonRegister(page: PageHolderFactory<*>) {
    get(page.path) {
        val page = try {
            coroutineScope {
                val job = coroutineContext[Job]!!
                val ctx = PageContextImpl(call, this, job)
                val page = page.create(ctx)
                try {
                    page.init(ctx)
                } catch(e: PageInitReturnedErrorStatusException) {
                    call.respond(HttpStatusCode.fromValue(e.httpStatus.code), e.detail.toString())
                }
                page
            }
        } catch (e: CancellationException) {
            if(!call.isHandled) {
                LOGGER.warn(e) { "Cancellation without response" }
                call.respond(HttpStatusCode.InternalServerError)
            }

            return@get
        }

        call.respondCompose {
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

fun Route.registerServerPages(fn: PageRegistry.() -> Unit) {
    (object : PageRegistry() {
        override fun template(fn: @Composable TemplateBuilder.() -> Unit) {
            pageTemplate = PageTemplate(fn)
        }

        override fun <R : PageHolder<R>, T : PageHolderFactory<R>> register(
            kClass: KClass<R>,
            kSerializer: KSerializer<R>,
            page: T
        ) {
            SerialRegistry.registerPage(kClass, kSerializer)
            commonRegister(page)
        }
    }).fn()
}
