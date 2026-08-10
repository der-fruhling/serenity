package net.derfruhling.serenity

import androidx.compose.runtime.*
import androidx.compose.runtime.tooling.CompositionObserver
import androidx.compose.runtime.tooling.ObservableComposition
import androidx.compose.runtime.tooling.setObserver
import js.numbers.JsNumbers.toKotlinDouble
import js.objects.Object
import js.objects.TypedPropertyDescriptor
import js.promise.Promise
import js.promise.await
import js.string.JsStrings.toKotlinString
import kotlinx.coroutines.*
import net.derfruhling.serenity.elements.currentPageLocal
import net.derfruhling.serenity.elements.pageTemplateLocal
import net.derfruhling.serenity.manifest.Manifest
import net.derfruhling.serenity.tree.HtmlCompositionContext
import net.derfruhling.serenity.tree.RehydratingHtmlTree
import net.derfruhling.serenity.tree.platform.*
import web.console.console
import web.dom.document
import web.events.EventHandler
import web.history.PopStateEvent
import web.history.history
import web.http.fetchAsync
import web.location.location
import web.prompts.alert
import web.time.DOMHighResTimeStamp
import web.url.URLSearchParams
import web.window.window
import kotlin.coroutines.resume
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This is a page entry point API and should not be called directly (unless you know what you're getting into)"
)
@MustBeDocumented
annotation class InternalPageEntryPoint

lateinit var htmlContext: HtmlCompositionContext private set
lateinit var currentPage: PageHolder<*> internal set
internal var compositionCompletionHandler: (() -> Unit)? = null

private val htmlContextStartHandlers = mutableListOf<(HtmlCompositionContext) -> Unit>()

@InternalPageEntryPoint
lateinit var htmlComposer: RehydratingHtmlTree<Document>
    private set

@InternalPageEntryPoint
@TestOnly
fun setHtmlComposerForTesting(ctx: HtmlCompositionContext, tree: RehydratingHtmlTree<Document>) {
    htmlContext = ctx
    htmlComposer = tree
}

fun onHtmlContextStart(fn: (HtmlCompositionContext) -> Unit) {
    htmlContextStartHandlers.add(fn)
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(get, set) => ({get, set})")
private external fun createSerenityDebugProperty(
    get: () -> Boolean,
    set: (Boolean) -> Unit
): TypedPropertyDescriptor<JsBoolean>

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => ({})")
private external fun emptyObject(): JsAny

@OptIn(ExperimentalWasmJsInterop::class)
internal class WebEntryPoint private constructor() {
    init {
        window.onpopstate = EventHandler { ev: PopStateEvent ->
            val state = ev.state
            if (state != null) {
                val page = SerialRegistry.decodeFromObject<SerialPageHolder>(state)
                navigateDirect(page as PageHolder<*>)
            }
        }

        Object.defineProperty(
            window, "serenityDebug", createSerenityDebugProperty(
                { htmlContext.enableDebugMode },
                { htmlContext.enableDebugMode = it }
            ))
    }

    private lateinit var manifest: Manifest
    private var first: Boolean = true
    private var initialized: Boolean = false
    private var clientMode by mutableStateOf(false)
    private val mutableStatePage = mutableStateOf<PageHolder<*>?>(null)
    private var page: PageHolder<*>? by mutableStatePage

    private lateinit var scope: CoroutineScope

    @OptIn(InternalPageEntryPoint::class)
    private fun initialize() {
        scope = CoroutineScope(Dispatchers.Main.immediate + AnimationFrameClock)
        scope.launch {
            console.log(Formatter.formatString(Document.CURRENT::format))

            htmlContext = HtmlCompositionContext(Recomposer(coroutineContext))
            htmlContextStartHandlers.forEach { it(htmlContext) }
            val manifestRequest: Promise<JsAny?> = if(htmlContext.enableTestMode) {
                Promise.resolve(emptyObject())
            } else {
                fetchAsync("/_/application-manifest.json").flatThen {
                    it.jsonAsync()
                }
            }

            if(location.hash.isNotEmpty()) {
                val properties = URLSearchParams(location.hash)
                when(properties.get("debug".toJsString())?.toKotlinString()) {
                    "", "yes", "true" -> htmlContext.enableDebugMode = true
                    "no", "false" -> htmlContext.enableDebugMode = false
                }
            }

            @OptIn(ExperimentalComposeRuntimeApi::class)
            if (htmlContext.enableDebugMode) {
                htmlComposer =
                    RehydratingHtmlTree(htmlContext.compositionContext, ::DebuggingHtmlApplier)
                htmlComposer.composition.setObserver(LoggingCompositionObserver)
            } else {
                htmlComposer = RehydratingHtmlTree(htmlContext.compositionContext)
                htmlComposer.composition.setObserver(CompletionObserver)
            }

            launch {
                console.debug("Entering recomposition loop")
                try {
                    htmlComposer.snapshot.enter {
                        htmlContext.compositionContext.runRecomposeAndApplyChanges()
                    }
                    console.warn("Composition loop exited normally???")
                } catch (_: CancellationException) {

                } catch (e: Exception) {
                    console.error("Recomposition loop stopped\n${e.stackTraceToString()}")
                    alert("An error has occurred: ${e::class.simpleName}\n${e.message}")
                }
            }

            manifest = try {
                val manifestJson = manifestRequest.await()
                SerialRegistry.decodeFromObject<Manifest>(manifestJson!!)
            } catch (e: Exception) {
                alert(
                    "An error occurred trying to fetch the application manifest: ${e::class.simpleName}\n${e.message}\n\n" +
                        "If you're the developer of this site, this error is likely due to a misconfiguration. " +
                        "Ensure you are serving the application-manifest.json file from your server."
                )
                Manifest(mutableMapOf())
            }

            initialized = true
            if(!htmlContext.enableTestMode) {
                // first apply what should be the server's version
                setPageContent()

                console.debug(htmlComposer.rootElement.dom)
                document.replaceChild(htmlComposer.rootElement.dom, document.documentElement)

                // then, update the tree with the client's version
                htmlComposer.snapshot.enter { clientMode = true }
            } else {
                // content is undefined in test mode
                htmlComposer.snapshot.enter { clientMode = true }
                setPageContent()
            }
        }

        first = false
    }

    @OptIn(InternalPageEntryPoint::class)
    private fun setPageContent() {
        try {
            htmlComposer.setContent {
                val manifestServices = remember(manifest) { manifest.provide }
                CompositionLocalProvider(*manifestServices) {
                    CompositionLocalProvider(
                        Manifest.local provides manifest,
                        isClientLocal provides clientMode,
                        pageTemplateLocal provides pageTemplate
                    ) {
                        pageTemplateLocal.current?.BuildPage(mutableStatePage)
                            ?: PageContent()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            alert("An error occurred: ${e::class.simpleName}\n${e.message}")
            throw e
        }
    }

    @Composable
    private fun PageContent() {
        CompositionLocalProvider(currentPageLocal provides page!!) {
            page!!.Main()
        }
    }

    internal fun setPage(page: PageHolder<*>) {
        setPageDirect(page)
        history.pushState(SerialRegistry.encodeToObject<SerialPageHolder>(page), "", page.path)
    }

    internal fun setPageDirect(page: PageHolder<*>) {
        currentPage = page
        this.page = page

        if (first) {
            initialize()
        }
    }

    @OptIn(InternalPageEntryPoint::class)
    fun tearDown() {
        first = true
        htmlComposer.close()
        scope.cancel()
    }

    companion object {
        val current by lazy { WebEntryPoint() }
    }
}

@InternalPageEntryPoint
fun invokeCommonEntryPoint(page: PageHolder<*>) {
    WebEntryPoint.current.setPage(page)
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(fn) => window.requestAnimationFrame(fn)")
private external fun windowRequestAnimationFrame(fn: (DOMHighResTimeStamp) -> Unit): Int

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(id) => window.cancelAnimationFrame(id)")
private external fun windowCancelAnimationFrame(id: Int)

@Suppress("UnnecessaryOptInAnnotation")
@OptIn(ExperimentalWasmJsInterop::class)
object AnimationFrameClock : MonotonicFrameClock {
    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
        return suspendCancellableCoroutine { continuation ->
            val id = windowRequestAnimationFrame {
                val duration = it.toKotlinDouble().toDuration(DurationUnit.MILLISECONDS)
                val result = onFrame(duration.inWholeNanoseconds)
                continuation.resume(result)
            }

            continuation.invokeOnCancellation {
                windowCancelAnimationFrame(id)
            }
        }
    }
}

private class DebuggingHtmlApplier(root: RootNode) : PlatformApplier(root) {
    override fun down(node: ComposeNode) {
        console.group("down(${Formatter.formatStringDebug(node::format)})")
        super.down(node)
    }

    override fun insertBottomUp(index: Int, instance: ComposeNode) {
        console.debug("insertBottomUp($index, ${Formatter.formatStringDebug(instance::format)})")
        super.insertBottomUp(index, instance)
    }

    override fun move(from: Int, to: Int, count: Int) {
        console.debug("move(from = $from, to = $to, count = $count)")
        super.move(from, to, count)
    }

    override fun remove(index: Int, count: Int) {
        console.debug("remove(index = $index, count = $count)")
        super.remove(index, count)
    }

    override fun up() {
        super.up()
        console.groupEnd()
    }

    override fun onBeginChanges() {
        console.groupCollapsed("Apply changes")
        super.onBeginChanges()
    }

    override fun reuse() {
        console.debug("reuse()")
        super.reuse()
    }

    override fun onEndChanges() {
        super.onEndChanges()
        console.groupEnd()
    }
}

@OptIn(ExperimentalComposeRuntimeApi::class)
private abstract class AbstractCompletionObserver : CompositionObserver {
    override fun onBeginComposition(composition: ObservableComposition) {}

    override fun onEndComposition(composition: ObservableComposition) {
        compositionCompletionHandler?.invoke()
    }

    override fun onReadInScope(scope: RecomposeScope, value: Any) {}

    override fun onScopeDisposed(scope: RecomposeScope) {}

    override fun onScopeEnter(scope: RecomposeScope) {}

    override fun onScopeExit(scope: RecomposeScope) {}

    override fun onScopeInvalidated(scope: RecomposeScope, value: Any?) {}
}

private object CompletionObserver : AbstractCompletionObserver()

@OptIn(ExperimentalComposeRuntimeApi::class)
private object LoggingCompositionObserver : AbstractCompletionObserver() {
    override fun onBeginComposition(composition: ObservableComposition) {
        console.groupCollapsed("onBeginComposition($composition)")
        super.onBeginComposition(composition)
    }

    override fun onScopeEnter(scope: RecomposeScope) {
        console.group("onScopeEnter($scope)")
        super.onScopeEnter(scope)
    }

    override fun onReadInScope(scope: RecomposeScope, value: Any) {
        console.debug("onReadInScope($scope, ${value::class.simpleName} :> $value)")
        super.onReadInScope(scope, value)
    }

    override fun onScopeExit(scope: RecomposeScope) {
        console.groupEnd()
        super.onScopeExit(scope)
    }

    override fun onEndComposition(composition: ObservableComposition) {
        console.groupEnd()
        super.onEndComposition(composition)
    }

    override fun onScopeInvalidated(scope: RecomposeScope, value: Any?) {
        console.debug("onScopeInvalidated($scope, ${value?.let { value::class.simpleName }} :> $value)")
        super.onScopeInvalidated(scope, value)
    }

    override fun onScopeDisposed(scope: RecomposeScope) {
        console.debug("onScopeDisposed($scope)")
        super.onScopeDisposed(scope)
    }
}
