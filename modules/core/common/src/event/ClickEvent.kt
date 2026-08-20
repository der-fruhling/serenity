package net.derfruhling.serenity.event

import kotlinx.serialization.Serializable

@Serializable
data object ClickEvent : EventType<ElementPointerEvent>("click"), BuiltinPointerEvent {
    @Serializable
    data object Double : EventType<ElementPointerEvent>("dblclick"), BuiltinPointerEvent
}
