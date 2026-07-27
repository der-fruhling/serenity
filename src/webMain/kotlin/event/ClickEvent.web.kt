package net.derfruhling.html.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.derfruhling.html.tree.platform.ElementNode
import web.events.Event
import web.dom.Element as DomElement

actual sealed interface ClickEvent : BaseEvent {
    private class Data(event: Event) : ClickEvent {
        override val element: ElementNode by lazy { ElementNode.tryGet(event.target as DomElement) }
    }

    @Serializable
    @SerialName("click")
    actual companion object Type : EventType<ClickEvent>("click") {
        override fun generate(event: Event): ClickEvent = Data(event)
    }
}
