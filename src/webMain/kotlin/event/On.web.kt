package net.derfruhling.html.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import net.derfruhling.html.InternalPageEntryPoint
import net.derfruhling.html.htmlComposer
import net.derfruhling.html.ifClient
import net.derfruhling.html.tree.HtmlApplier
import net.derfruhling.html.tree.platform.EventHandlerNode

@OptIn(InternalPageEntryPoint::class)
@Composable
actual fun <T> On(type: EventType<T>, fn: (T) -> Unit) {
    val lambda = { e: T ->
        htmlComposer.snapshot.enter {
            fn(e)
        }
    }

    ifClient {
        ComposeNode<EventHandlerNode<T>, HtmlApplier>(::EventHandlerNode, update = {
            set(type) { this.type = type }
            set(lambda) { this.fn = lambda }
        })
    }
}
