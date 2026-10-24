package net.derfruhling.serenity.platform

import net.derfruhling.serenity.Formatter
import net.derfruhling.serenity.event.EventType

abstract class AbstractEventHandlerNode<T> : ComposeNode,
    ChildNode<NodeWithChildren<*, *>> {
    abstract var type: EventType<T>
}

expect class EventHandlerNode<T>() : AbstractEventHandlerNode<T> {
    override var type: EventType<T>
    var fn: (T) -> Unit

    override val index: Index<EventHandlerNode<T>>

    override var parent: NodeWithChildren<*, *>?
    override fun reparent(newParent: NodeWithChildren<*, *>)

    override fun format(fmt: Formatter)

    override fun deepCopy(): EventHandlerNode<T>
}
