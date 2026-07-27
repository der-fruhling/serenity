package net.derfruhling.html.ktor.server

import androidx.compose.runtime.*
import co.touchlab.kermit.Logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import net.derfruhling.html.annotations.HtmlComposable
import net.derfruhling.html.tree.HtmlCompositionContext
import net.derfruhling.html.tree.RehydratingHtmlTree
import net.derfruhling.html.tree.composeHtmlOnce
import net.derfruhling.html.tree.encodeToString
import net.derfruhling.html.tree.platform.Document
import net.derfruhling.html.tree.platform.PlatformApplier
import kotlin.coroutines.CoroutineContext

private val logger = Logger.withTag("net.derfruhling.html.ktor.server.ComposeHtmlPluginKt")

interface ComposeHtmlConfig {
    fun registerTransformation(fn: Transformer)
}

fun interface Transformer {
    fun ApplicationCall.transform(tree: Document): Document
    fun ApplicationCall.cacheKey(tree: Document): Int? = null
}

private class ComposeHtmlConfigImpl : ComposeHtmlConfig {
    val transformations = mutableListOf<Transformer>()

    override fun registerTransformation(fn: Transformer) {
        transformations.add(fn)
        logger.i { "Registered transformation: $fn" }
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

    logger.i { "ComposeHTML initialized" }
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

    respondText(ContentType.Text.Html) { doc.encodeToString() }
}
