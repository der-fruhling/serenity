package net.derfruhling.html.event

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import web.events.Event

@Serializable
@Polymorphic
@Immutable
actual abstract class EventType<T>(actual val name: String) {
    abstract fun generate(event: Event): T
}
