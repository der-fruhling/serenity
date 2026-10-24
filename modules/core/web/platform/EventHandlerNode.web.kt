package net.derfruhling.serenity.platform

import androidx.compose.runtime.ComposeNodeLifecycleCallback
import net.derfruhling.serenity.Formatter
import net.derfruhling.serenity.event.EventSubscriptionHandle
import net.derfruhling.serenity.event.EventType
import kotlin.properties.Delegates

actual class EventHandlerNode<T> : AbstractEventHandlerNode<T>(), ComposeNodeLifecycleCallback {
    actual override var type: EventType<T> by Delegates.notNull()
    actual var fn: (T) -> Unit = {}
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EventHandlerNode<*>) return false

        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }

    actual override fun deepCopy(): EventHandlerNode<T> {
        return EventHandlerNode<T>().also {
            try {
                it.type = type
            } catch (_: IllegalStateException) {

            }

            it.fn = fn
        }
    }
}