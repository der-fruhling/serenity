package net.derfruhling.serenity.ktor.server

import androidx.compose.runtime.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.engine.*
import io.ktor.server.http.*
import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.io.files.Path
import net.derfruhling.serenity.SerialRegistry
import net.derfruhling.serenity.annotations.HtmlComposable
import net.derfruhling.serenity.manifest.Manifest
import net.derfruhling.serenity.manifest.Preload
import net.derfruhling.serenity.manifest.preloadSetLocal
import net.derfruhling.serenity.tree.HtmlCompositionContext
import net.derfruhling.serenity.tree.RehydratingHtmlTree
import net.derfruhling.serenity.tree.encodeToString
import net.derfruhling.serenity.tree.platform.Document
import net.derfruhling.serenity.tree.platform.PlatformApplier

private val logger = KotlinLogging.logger {}

interface ComposeHtmlConfig {
    var manifestPath: Path
    var manifestAlwaysInFileSystem: Boolean

    fun registerTransformation(fn: Transformer)
}

fun interface Transformer {
    fun ApplicationCall.transform(tree: Document): Document
    fun ApplicationCall.cacheKey(tree: Document): Int? = null
}

private class ComposeHtmlConfigImpl : ComposeHtmlConfig {
    override var manifestPath: Path = Path("application-manifest.json")
    override var manifestAlwaysInFileSystem: Boolean = false
    val transformations = mutableListOf<Transformer>()

    override fun registerTransformation(fn: Transformer) {
        transformations.add(fn)
        logger.info { "Registered transformation: $fn" }
    }
}

expect fun ComposeHtmlConfig.readManifest(): String

class KtorHtmlCompositionContext(
    recomposer: Recomposer,
    val transformations: ImmutableList<Transformer>
) : HtmlCompositionContext(recomposer) {
    inline fun transform(
        call: ApplicationCall,
        tree: Document,
        cacheKeyAcceptor: (Int) -> Unit
    ): Document {
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
private val manifestKey = AttributeKey<Manifest>("serenityManifest")
internal val staticFilePath = AttributeKey<String>("staticFilePath")

val ApplicationCall.compositionContext: KtorHtmlCompositionContext
    get() = attributes[contextKey]

val ApplicationCall.currentManifest: Manifest
    get() = attributes[manifestKey]

val ComposeHtml = createApplicationPlugin(
    "ComposeHtml",
    { ComposeHtmlConfigImpl() as ComposeHtmlConfig }
) {
    val impl = pluginConfig as ComposeHtmlConfigImpl
    val context =
        KtorHtmlCompositionContext(
            Recomposer(application.coroutineContext),
            impl.transformations.toImmutableList()
        )

    val manifestText = impl.readManifest()
    val manifest = SerialRegistry.decode<Manifest>(manifestText)

    on(CallSetup) {
        it.attributes[contextKey] = context
        it.attributes[manifestKey] = manifest
    }

    application.install(ConditionalHeaders)

    val serializedManifest by lazy { SerialRegistry.encode(manifest) }

    application.routing {
        get("/_/application-manifest.json") {
            call.respondText(ContentType.Application.Json, HttpStatusCode.OK) {
                serializedManifest
            }
        }
    }

    logger.info { "Serenity framework initialized" }
}

suspend inline fun ApplicationCall.respondCompose(crossinline fn: @Composable @HtmlComposable () -> Unit) {
    val context = compositionContext
    val tree = RehydratingHtmlTree(context.compositionContext, request.uri, ::PlatformApplier)
    val preloadSet = mutableSetOf<Preload>()

    tree.setContent {
        val call = remember { this }
        val manifest = remember { call.currentManifest }

        CompositionLocalProvider(*manifest.provide) {
            CompositionLocalProvider(
                applicationCallLocal provides call,
                preloadSetLocal provides preloadSet
            ) {
                fn()
            }
        }
    }

    for ((href, `as`) in preloadSet) {
        response.link(
            LinkHeader(
                href, listOf(
                    HeaderValueParam("rel", "preload"),
                    HeaderValueParam("as", `as`)
                )
            )
        )
    }

    val doc = context.transform(this, tree.root) { /* TODO */ }

    respondText(ContentType.Text.Html) { doc.encodeToString() }
}

expect fun <E : ApplicationEngine, C : ApplicationEngine.Configuration> EmbeddedServer<E, C>.startAwait()
