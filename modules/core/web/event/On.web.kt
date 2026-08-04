package net.derfruhling.serenity.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import net.derfruhling.serenity.InternalPageEntryPoint
import net.derfruhling.serenity.SnapshotContext
import net.derfruhling.serenity.htmlComposer
import net.derfruhling.serenity.ifClient
import net.derfruhling.serenity.tree.HtmlApplier
import net.derfruhling.serenity.tree.platform.EventHandlerNode
import kotlin.coroutines.CoroutineContext

actual class EventContext internal constructor(val snapshot: Snapshot, val eventType: EventType<*>) : CoroutineScope {
    private var jobInitialized = false
    internal val job: Job by lazy { Job().also { jobInitialized = true } }

    actual override val coroutineContext: CoroutineContext by lazy {
        Dispatchers.Default + SnapshotContext(snapshot) + job
    }
}

@OptIn(InternalPageEntryPoint::class)
@Composable
actual fun <T> On(type: EventType<T>, fn: EventContext.(T) -> Unit) {
    val lambda = { e: T ->
        htmlComposer.snapshot.enter {
            EventContext(htmlComposer.snapshot, type).fn(e)
        }
    }

    ifClient {
        ComposeNode<EventHandlerNode<T>, HtmlApplier>(::EventHandlerNode, update = {
            set(type) { this.type = type }
            set(lambda) { this.fn = lambda }
        })
    }
}
