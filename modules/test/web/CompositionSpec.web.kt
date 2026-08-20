package net.derfruhling.serenity.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import net.derfruhling.serenity.*
import net.derfruhling.serenity.HtmlComposable
import net.derfruhling.serenity.elements.Page
import net.derfruhling.serenity.tree.HtmlCompositionContext
import net.derfruhling.serenity.tree.RehydratingHtmlTree
import net.derfruhling.serenity.tree.platform.PlatformApplier
import net.derfruhling.serenity.tree.platform.RealDocument
import web.console.console
import web.dom.Document
import web.dom.document
import net.derfruhling.serenity.tree.platform.Document as PlatformDocument

internal actual suspend inline fun <T> withFrameClock(crossinline fn: suspend CoroutineScope.() -> T): T {
    return withContext(AnimationFrameClock) {
        fn()
    }
}

class DomComposeContext(
    val recomposer: Recomposer,
    val tree: RehydratingHtmlTree<PlatformDocument>
) {
    suspend fun awaitIdle() = recomposer.awaitIdle()

    inline fun useSnapshot(fn: () -> Unit) = tree.snapshot.enter { fn() }
}

@OptIn(InternalPageEntryPoint::class)
suspend fun runDomComposeTest(
    fn: @Composable @HtmlComposable () -> Unit,
    after: suspend DomComposeContext.(Document) -> Unit
) = withContext(AnimationFrameClock + SnapshotContext(Snapshot.takeMutableSnapshot())) {
    val recomposer = Recomposer(currentCoroutineContext())
    val document = document.implementation.createHTMLDocument()
    val tree = RehydratingHtmlTree(recomposer, PlatformDocument(RealDocument(document)), ::PlatformApplier)
    setHtmlComposerForTesting(HtmlCompositionContext(recomposer), tree)

    tree.setContent {
        Page {
            Head {}
            Body { fn() }
        }
    }

    launch { recomposer.runRecomposeAndApplyChanges() }

    try {
        console.log("before yield")
        yield()
        console.log("after yield")
        recomposer.awaitIdle()
        console.log("after awaitIdle")
        DomComposeContext(recomposer, tree).after(document)
        console.log("after all")
    } finally {
        console.log("before cancel")
        recomposer.close()
        recomposer.join()
        console.log("after cancel")
    }
}
