package net.derfruhling.html

import androidx.compose.runtime.*
import androidx.compose.runtime.tooling.CompositionObserver
import androidx.compose.runtime.tooling.ObservableComposition
import androidx.compose.runtime.tooling.setObserver
import js.numbers.JsNumbers.toKotlinDouble
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import net.derfruhling.html.tree.HtmlCompositionContext
import net.derfruhling.html.tree.RehydratingHtmlTree
import net.derfruhling.html.tree.platform.*
import web.console.console
import web.dom.document
import web.prompts.alert
import web.time.DOMHighResTimeStamp
import kotlin.coroutines.resume
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "This is a page entry point API and should not be called directly (unless you know what you're getting into)")
@MustBeDocumented
annotation class InternalPageEntryPoint

lateinit var htmlContext: HtmlCompositionContext
    private set

private val htmlContextStartHandlers = mutableListOf<(HtmlCompositionContext) -> Unit>()

@InternalPageEntryPoint
lateinit var htmlComposer: RehydratingHtmlTree<Document>
    private set

fun onHtmlContextStart(fn: (HtmlCompositionContext) -> Unit) {
    htmlContextStartHandlers.add(fn)
}

@OptIn(ExperimentalWasmJsInterop::class)
@InternalPageEntryPoint
fun invokeCommonEntryPoint(page: PageHolder) {
    CoroutineScope(Dispatchers.Main.immediate + AnimationFrameClock).launch {
        console.log(Formatter.formatString(Document.CURRENT::format))
        var clientMode by mutableStateOf(false)

        htmlContext = HtmlCompositionContext(Recomposer(coroutineContext))
        htmlContextStartHandlers.forEach { it(htmlContext) }

        @OptIn(ExperimentalComposeRuntimeApi::class)
        if(htmlContext.enableDebugMode) {
            htmlComposer = RehydratingHtmlTree(htmlContext.compositionContext, ::DebuggingHtmlApplier)
            htmlComposer.composition.setObserver(LoggingCompositionObserver)
        } else {
            htmlComposer = RehydratingHtmlTree(htmlContext.compositionContext)
        }

        launch {
            console.debug("Entering recomposition loop")
            try {
                htmlComposer.snapshot.enter {
                    htmlContext.compositionContext.runRecomposeAndApplyChanges()
                }
                console.warn("Composition loop exited normally???")
            } catch (e: Exception) {
                console.error("Recomposition loop stopped\n${e.stackTraceToString()}")
                alert("An error has occurred: ${e::class.simpleName}\n${e.message}")
            }
        }

        // first apply what should be the server's version
        try {
            htmlComposer.setContent {
                withCompositionLocal(
                    isClientLocal provides clientMode
                ) {
                    page.Main()
                }
            }
        } catch (e: Exception) {
            alert("An error occurred: ${e::class.simpleName}\n${e.message}")
            return@launch
        }

        console.debug(htmlComposer.rootElement.dom)
        document.replaceChild(htmlComposer.rootElement.dom, document.documentElement)

        // then, update the tree with the client's version
        htmlComposer.snapshot.enter { clientMode = true }
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(fn) => window.requestAnimationFrame(fn)")
private external fun windowRequestAnimationFrame(fn: (DOMHighResTimeStamp) -> Unit): Int

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(id) => window.cancelAnimationFrame(id)")
private external fun windowCancelAnimationFrame(id: Int)

private object AnimationFrameClock : MonotonicFrameClock {
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
private object LoggingCompositionObserver : CompositionObserver {
    override fun onBeginComposition(composition: ObservableComposition) {
        console.groupCollapsed("onBeginComposition($composition)")
    }

    override fun onScopeEnter(scope: RecomposeScope) {
        console.group("onScopeEnter($scope)")
    }

    override fun onReadInScope(scope: RecomposeScope, value: Any) {
        console.debug("onReadInScope($scope, $value)")
    }

    override fun onScopeExit(scope: RecomposeScope) {
        console.groupEnd()
    }

    override fun onEndComposition(composition: ObservableComposition) {
        console.groupEnd()
    }

    override fun onScopeInvalidated(scope: RecomposeScope, value: Any?) {
        console.debug("onScopeInvalidated($scope, $value)")
    }

    override fun onScopeDisposed(scope: RecomposeScope) {
        console.debug("onScopeDisposed($scope)")
    }
}
