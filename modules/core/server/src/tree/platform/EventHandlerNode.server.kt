package net.derfruhling.serenity.tree.platform

import net.derfruhling.serenity.Formatter
import net.derfruhling.serenity.event.EventType
import kotlin.properties.Delegates

actual class EventHandlerNode<T> : AbstractEventHandlerNode<T>() {
    actual var type: EventType<T> by Delegates.notNull()
    actual inline var fn: (T) -> Unit
        get() = {}
        set(value) {}
    actual override val index: Index<EventHandlerNode<T>> = Index(this)
    actual override var parent: NodeWithChildren<*, *>? = null

    actual override fun reparent(newParent: NodeWithChildren<*, *>) {
        parent = newParent
    }

    actual override fun format(fmt: Formatter) {
        if (fmt.deepIntrospect) {
            fmt.enter(Formatter.Begin.DEBUG, "EventHandler") {
                write(type.name)
            }
        }
    }
}
