package net.derfruhling.html.tree.platform

import net.derfruhling.html.Formatter
import net.derfruhling.html.event.EventType

abstract class AbstractEventHandlerNode<T> : ComposeNode, ChildNode<NodeWithChildren<*>>, DisallowReuse

expect class EventHandlerNode<T> : AbstractEventHandlerNode<T> {
    var type: EventType<T>
    var fn: (T) -> Unit

    override val index: Index<EventHandlerNode<T>>

    override var parent: NodeWithChildren<*>?
    override fun reparent(newParent: NodeWithChildren<*>)

    override fun format(fmt: Formatter)
}
