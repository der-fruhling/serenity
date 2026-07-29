package net.derfruhling.serenity.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.derfruhling.serenity.InternalPageEntryPoint
import net.derfruhling.serenity.SnapshotContext
import net.derfruhling.serenity.htmlComposer
import net.derfruhling.serenity.ifClient
import net.derfruhling.serenity.tree.HtmlApplier
import net.derfruhling.serenity.tree.platform.EventHandlerNode
import web.function.async
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

@OptIn(InternalPageEntryPoint::class)
@Composable
actual fun <T> On(type: EventType<T>, fn: EventContext.(T) -> Unit) {
    val lambda = { e: T ->
        htmlComposer.snapshot.enter {
            EventContext(htmlComposer.snapshot).fn(e)
        }
    }

    ifClient {
        ComposeNode<EventHandlerNode<T>, HtmlApplier>(::EventHandlerNode, update = {
            set(type) { this.type = type }
            set(lambda) { this.fn = lambda }
        })
    }
}

actual class EventContext internal constructor(snapshot: Snapshot) : CoroutineScope {
    actual override val coroutineContext: CoroutineContext = Dispatchers.Default + SnapshotContext(snapshot)
}
