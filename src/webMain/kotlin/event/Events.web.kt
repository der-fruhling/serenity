package net.derfruhling.serenity.event

import kotlinx.serialization.Serializable
import net.derfruhling.serenity.tree.platform.ElementNode
import web.events.Event
import web.pointer.PointerEvent as DomPointerEvent

@Serializable
actual sealed interface BuiltinEventType

@Serializable
actual sealed interface BuiltinPointerEvent : BuiltinEventType, GenerateFromDomEvent<PointerEvent<ElementNode>> {
    override fun generate(event: Event): PointerEvent<ElementNode> {
        return (event as DomPointerEvent).asComposeEvent()
    }
}
