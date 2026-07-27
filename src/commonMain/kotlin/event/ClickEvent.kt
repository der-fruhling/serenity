package net.derfruhling.html.event

import kotlinx.serialization.Serializable

expect sealed interface ClickEvent : BaseEvent {
    @Serializable
    companion object Type : EventType<ClickEvent>
}
