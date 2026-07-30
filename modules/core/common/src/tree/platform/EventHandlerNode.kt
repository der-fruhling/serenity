package net.derfruhling.serenity.tree.platform

import net.derfruhling.serenity.Formatter
import net.derfruhling.serenity.event.EventType

abstract class AbstractEventHandlerNode<T> : ComposeNode, ChildNode<NodeWithChildren<*, *>>, DisallowReuse

expect class EventHandlerNode<T> : AbstractEventHandlerNode<T> {
    var type: EventType<T>
    var fn: (T) -> Unit

    override val index: Index<EventHandlerNode<T>>

    override var parent: NodeWithChildren<*, *>?
    override fun reparent(newParent: NodeWithChildren<*, *>)

    override fun format(fmt: Formatter)
}
