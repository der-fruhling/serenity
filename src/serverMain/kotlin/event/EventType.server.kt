package net.derfruhling.html.event

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
@Polymorphic
@Immutable
actual abstract class EventType<T> actual constructor(actual val name: String)
