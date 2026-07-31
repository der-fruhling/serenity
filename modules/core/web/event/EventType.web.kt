package net.derfruhling.serenity.event

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import web.events.Event

sealed interface GenerateFromDomEvent<T> {
    fun generate(event: Event): T
}

@Serializable
@Polymorphic
@Immutable
actual abstract class EventType<T> actual constructor(actual val name: String) :
    GenerateFromDomEvent<T> {
    actual open val isSupported: Boolean
        get() = true
}
