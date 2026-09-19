package net.derfruhling.serenity.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.*
import net.derfruhling.serenity.AnimationFrameClock
import net.derfruhling.serenity.HtmlComposable
import net.derfruhling.serenity.InternalPageEntryPoint
import net.derfruhling.serenity.elements.Page
import net.derfruhling.serenity.platform.CURRENT
import net.derfruhling.serenity.platform.HtmlCompositionContext
import net.derfruhling.serenity.platform.PlatformApplier
import net.derfruhling.serenity.platform.RehydratingHtmlTree
import net.derfruhling.serenity.serial.SnapshotContext
import net.derfruhling.serenity.setHtmlComposerForTesting
import web.dom.Document
import web.dom.document
import net.derfruhling.serenity.platform.Document as PlatformDocument

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
}

@OptIn(InternalPageEntryPoint::class)
suspend fun runDomComposeTest(
    fn: @Composable @HtmlComposable () -> Unit,
    after: suspend DomComposeContext.(Document) -> Unit
) {
    val snapshot = Snapshot.takeMutableSnapshot()
    withContext(AnimationFrameClock + SnapshotContext(snapshot)) {
        val recomposer = Recomposer(currentCoroutineContext())
        val tree =
            RehydratingHtmlTree(recomposer, PlatformDocument.CURRENT, ::PlatformApplier, snapshot)
        setHtmlComposerForTesting(HtmlCompositionContext(recomposer), tree)

        tree.setContent {
            Page {
                Head {}
                Body { fn() }
            }
        }

        launch { recomposer.runRecomposeAndApplyChanges() }

        try {
            yield()
            recomposer.awaitIdle()
            DomComposeContext(recomposer, tree).after(document)
        } finally {
            recomposer.close()
            recomposer.join()
        }
    }
}
