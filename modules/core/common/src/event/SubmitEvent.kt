package net.derfruhling.serenity.event

import kotlinx.serialization.Serializable
import net.derfruhling.serenity.dom.Element

@Serializable
data object SubmitEvent : EventType<Event<Element>>("submit"), BuiltinPlainElementEvent
