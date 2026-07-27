package net.derfruhling.html.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

actual sealed interface ClickEvent : BaseEvent {
    @Serializable
    @SerialName("click")
    actual companion object Type : EventType<ClickEvent>("click")
}
