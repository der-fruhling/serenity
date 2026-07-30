package net.derfruhling.serenity.event

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
@Polymorphic
@Immutable
expect abstract class EventType<T> {
    val name: String
    open val isSupported: Boolean

    constructor(name: String)
}
