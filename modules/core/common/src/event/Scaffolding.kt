package net.derfruhling.serenity.event

import kotlinx.serialization.Serializable

@Serializable
expect sealed interface BuiltinEventType

@Serializable
expect sealed interface BuiltinPlainElementEvent : BuiltinEventType

@Serializable
expect sealed interface BuiltinPlainWindowEvent : BuiltinEventType

@Serializable
expect sealed interface BuiltinPlainDocumentEvent : BuiltinEventType

@Serializable
expect sealed interface BuiltinPointerEvent : BuiltinEventType

@Serializable
expect sealed interface BuiltinPageTransitionEvent : BuiltinEventType

expect fun testSupportedDocumentEvent(name: String): Boolean
expect fun testSupportedWindowEvent(name: String): Boolean
