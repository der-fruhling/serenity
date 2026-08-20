package net.derfruhling.serenity.event

import kotlinx.serialization.Serializable
import net.derfruhling.serenity.dom.Window

@Serializable
data object BeforeUnloadEvent : EventType<Event<Window>>("beforeunload"), BuiltinPlainWindowEvent
