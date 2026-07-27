package net.derfruhling.html.ktor.server

import androidx.compose.runtime.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.http.link
import io.ktor.server.plugins.conditionalheaders.ConditionalHeaders
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import net.derfruhling.html.Name
import net.derfruhling.html.SerialRegistry
import net.derfruhling.html.annotations.HtmlComposable
import net.derfruhling.html.attribute.Attributes
import net.derfruhling.html.tree.HtmlCompositionContext
import net.derfruhling.html.tree.RehydratingHtmlTree
import net.derfruhling.html.tree.encodeToString
import net.derfruhling.html.tree.platform.Document
import net.derfruhling.html.tree.platform.ElementNode
import net.derfruhling.html.tree.platform.PlatformApplier
import net.derfruhling.html.tree.platform.TextNode
import net.derfruhling.html.tree.platform.head

private val logger = KotlinLogging.logger {}

interface ComposeHtmlConfig {
    fun registerTransformation(fn: Transformer)

    fun useScript(uri: String, async: Boolean = false, defer: Boolean = false, preload: Boolean = uri.startsWith('/')) {
        registerTransformation {
            if(preload) {
                response.link(LinkHeader(uri, listOf(
                    HeaderValueParam("rel", "preload"),
                    HeaderValueParam("as", "script")
                )))

                val titleIndex = it.head!!.findImmediateElementIndexNamed(Name.of("title"))
                if(titleIndex != -1) {
                    it.head!!.insert(titleIndex + 1, ElementNode("link").apply {
                        attribute(Attributes.rel, "preload")
                        attribute(Attributes.`as`, "script")
                        attribute(Attributes.href, uri)
                    })
                }
            }

            it.head!!.add(ElementNode("script").apply {
                if(async) attribute(Attributes.async, null)
                if(defer) attribute(Attributes.defer, null)
                attribute(Attributes.src, uri)
            })

            it
        }
    }

    fun useEntrypoint(projectName: String) {
        registerTransformation {
            val hash = attributes[pageFunctionName]

            it.head!!.add(ElementNode("script").apply element@ {
                //language=javascript
                add(TextNode("""
                    const entryFn = window["$projectName"]["$hash"];
                    if(entryFn) entryFn();
                    else console.warn("Entry function for", "$hash", "not found");
                """.trimIndent()))
            })

            it
        }
    }
}

fun interface Transformer {
    fun ApplicationCall.transform(tree: Document): Document
    fun ApplicationCall.cacheKey(tree: Document): Int? = null
}

private class ComposeHtmlConfigImpl : ComposeHtmlConfig {
    val transformations = mutableListOf<Transformer>()

    override fun registerTransformation(fn: Transformer) {
        transformations.add(fn)
        logger.info { "Registered transformation: $fn" }
    }
}

class KtorHtmlCompositionContext(
    recomposer: Recomposer,
    val transformations: ImmutableList<Transformer>
) : HtmlCompositionContext(recomposer) {
    inline fun transform(call: ApplicationCall, tree: Document, cacheKeyAcceptor: (Int) -> Unit): Document {
        if (transformations.isEmpty()) return tree

        return transformations.fold(tree) { tree, t ->
            with(t) {
                call.transform(tree).also {
                    call.cacheKey(it)?.let { key -> cacheKeyAcceptor(key) }
                }
            }
        }
    }
}

val applicationCallLocal = compositionLocalOf<ApplicationCall> { throw NotImplementedError() }

inline val currentCall: ApplicationCall
    @Composable
    @ReadOnlyComposable
    inline get() = applicationCallLocal.current

private val contextKey = AttributeKey<KtorHtmlCompositionContext>("htmlCompositionContext")
internal val staticFilePath = AttributeKey<String>("staticFilePath")

val ApplicationCall.compositionContext: KtorHtmlCompositionContext
    get() = attributes[contextKey]

val ComposeHtml = createApplicationPlugin(
    "ComposeHtml",
    { ComposeHtmlConfigImpl() as ComposeHtmlConfig }
) {
    val impl = pluginConfig as ComposeHtmlConfigImpl
    val context = KtorHtmlCompositionContext(Recomposer(application.coroutineContext), impl.transformations.toImmutableList())

    on(CallSetup) {
        it.attributes[contextKey] = context
    }

    application.install(ConditionalHeaders)

    logger.info { "ComposeHTML initialized" }
}

suspend inline fun ApplicationCall.respondCompose(crossinline fn: @Composable @HtmlComposable () -> Unit) {
    val context = compositionContext
    val tree = RehydratingHtmlTree(context.compositionContext, request.uri, ::PlatformApplier)

    tree.setContent {
        val call = remember { this }

        CompositionLocalProvider(
            applicationCallLocal provides call
        ) {
            fn()
        }
    }

    val doc = context.transform(this, tree.root) { /* TODO */ }
    val save = tree.save()

    if(!save.isEmpty) {
        doc.head!!.add(ElementNode("script").apply {
            attribute(Attributes.type, "application/json+x-compose-shared")
            add(TextNode(SerialRegistry.encode(save).replace("<", "\\u003e")))
        })
    }

    respondText(ContentType.Text.Html) { doc.encodeToString() }
}

expect fun <E : ApplicationEngine, C: ApplicationEngine.Configuration> EmbeddedServer<E, C>.startAwait()
