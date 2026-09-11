package net.derfruhling.serenity.server.ktor

import androidx.compose.runtime.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.events.Events
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
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import net.derfruhling.serenity.SerialRegistry
import net.derfruhling.serenity.HtmlComposable
import net.derfruhling.serenity.manifest.Manifest
import net.derfruhling.serenity.manifest.Preload
import net.derfruhling.serenity.manifest.preloadSetLocal
import net.derfruhling.serenity.modularity.CommonContext
import net.derfruhling.serenity.modularity.Modules
import net.derfruhling.serenity.modularity.ProvideContext
import net.derfruhling.serenity.modularity.ServerContext
import net.derfruhling.serenity.platform.HtmlCompositionContext
import net.derfruhling.serenity.platform.RehydratingHtmlTree
import net.derfruhling.serenity.platform.encodeToString
import net.derfruhling.serenity.platform.Document
import net.derfruhling.serenity.platform.PlatformApplier

private val logger = KotlinLogging.logger {}

interface SerenityConfig {
    var manifestPath: Path
    var manifestAlwaysInFileSystem: Boolean

    fun registerTransformation(fn: Transformer)
}

fun interface Transformer {
    fun ApplicationCall.transform(tree: Document): Document
    fun ApplicationCall.cacheKey(tree: Document): Int? = null
}

private class SerenityConfigImpl : SerenityConfig {
    override var manifestPath: Path = Path("application-manifest.json")
    override var manifestAlwaysInFileSystem: Boolean = false
    val transformations = mutableListOf<Transformer>()

    override fun registerTransformation(fn: Transformer) {
        transformations.add(fn)
        logger.info { "Registered transformation: $fn" }
    }
}

expect fun SerenityConfig.readManifest(): String

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

private val contextKey = AttributeKey<KtorHtmlCompositionContext>("serenityCompositionContext")
private val manifestKey = AttributeKey<Manifest>("serenityManifest")
internal val staticFilePath = AttributeKey<String>("staticFilePath")

val ApplicationCall.compositionContext: KtorHtmlCompositionContext
    get() = attributes[contextKey]

val ApplicationCall.currentManifest: Manifest
    get() = attributes[manifestKey]

val Serenity = createApplicationPlugin(
    "Serenity",
    { SerenityConfigImpl() as SerenityConfig }
) {
    val impl = pluginConfig as SerenityConfigImpl
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

    application.monitor.subscribe(ApplicationStarted) {
        it.launch {
            Modules.asyncInit(CommonContextImpl(manifest))
        }
    }

    logger.info { "Serenity framework initialized" }
}

private open class CommonContextImpl(private val manifest: Manifest) : CommonContext {
    constructor(call: ApplicationCall) : this(call.currentManifest)

    override suspend fun getManifest(): Manifest {
        return manifest
    }
}

private open class ServerContextImpl(protected val call: ApplicationCall) : CommonContextImpl(call), ServerContext {
    override fun getHeader(name: String): String? {
        return call.request.header(name)
    }
}

private class ProvideContextImpl(call: ApplicationCall) : ServerContextImpl(call), ProvideContext {
    private val list = mutableListOf<ProvidedValue<*>>()

    override suspend fun use(provide: ProvidedValue<*>) {
        list.add(provide)
    }

    fun build(): Array<ProvidedValue<*>> = list.toTypedArray()
}

suspend fun ApplicationCall.createModuleProvidedValues(): Array<ProvidedValue<*>> {
    return ProvideContextImpl(this).also { Modules.createProvidedValues(it) }.build()
}

suspend inline fun ApplicationCall.respondCompose(crossinline fn: @Composable @HtmlComposable () -> Unit) {
    val context = compositionContext
    val tree = RehydratingHtmlTree(context.compositionContext, request.uri, ::PlatformApplier)
    val preloadSet = mutableSetOf<Preload>()
    val moduleProvidedState = createModuleProvidedValues()

    tree.setContent {
        val call = remember { this }
        val manifest = remember { call.currentManifest }

        CompositionLocalProvider(*manifest.provide + moduleProvidedState) {
            CompositionLocalProvider(
                applicationCallLocal provides call,
                preloadSetLocal provides preloadSet
            ) {
                fn()
            }
        }
    }

    for ((href, `as`, crossorigin) in preloadSet) {
        response.link(
            LinkHeader(
                href, listOfNotNull(
                    HeaderValueParam("rel", "preload"),
                    HeaderValueParam("as", `as`),
                    if (crossorigin) HeaderValueParam("crossorigin", "") else null
                )
            )
        )
    }

    val doc = context.transform(this, tree.root) { /* TODO */ }

    respondText(ContentType.Text.Html) { doc.encodeToString() }
}

expect fun <E : ApplicationEngine, C : ApplicationEngine.Configuration> EmbeddedServer<E, C>.startAwait()
