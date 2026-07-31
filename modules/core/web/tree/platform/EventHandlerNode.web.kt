package net.derfruhling.serenity.tree.platform

import androidx.compose.runtime.ComposeNodeLifecycleCallback
import net.derfruhling.serenity.Formatter
import net.derfruhling.serenity.event.EventSubscriptionHandle
import net.derfruhling.serenity.event.EventType
import kotlin.properties.Delegates

actual class EventHandlerNode<T> : AbstractEventHandlerNode<T>(), ComposeNodeLifecycleCallback {
    actual var type: EventType<T> by Delegates.notNull()
    actual var fn: (T) -> Unit by Delegates.notNull()
    actual override val index: Index<EventHandlerNode<T>> = Index(this)
    actual override var parent: NodeWithChildren<*, *>? = null

    actual override fun reparent(newParent: NodeWithChildren<*, *>) {
        parent = newParent
    }

    private lateinit var handle: EventSubscriptionHandle

    override fun applied() {
        handle = (parent as ElementNode).subscribe(type, fn)
    }

    override fun onDeactivate() {
        handle.unsubscribe()
    }

    override fun onRelease() {}
    override fun onReuse() {}

    actual override fun format(fmt: Formatter) {
        if (fmt.deepIntrospect) {
            fmt.enter(Formatter.Begin.DEBUG, "EventHandler") {
                write(
                    try {
                        type.name
                    } catch (_: IllegalStateException) {
                        "<???>"
                    }
                )
            }
        }
    }
}