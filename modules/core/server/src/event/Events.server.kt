package net.derfruhling.serenity.event

import kotlinx.serialization.Serializable

@Serializable
actual sealed interface BuiltinEventType

@Serializable
actual sealed interface BuiltinPointerEvent : BuiltinEventType

@Serializable
actual sealed interface BuiltinPlainWindowEvent : BuiltinEventType

@Serializable
actual sealed interface BuiltinPlainDocumentEvent : BuiltinEventType

@Serializable
actual sealed interface BuiltinPageTransitionEvent : BuiltinEventType

@Suppress("NOTHING_TO_INLINE")
actual inline fun testSupportedDocumentEvent(name: String): Boolean {
    return false
}

@Suppress("NOTHING_TO_INLINE")
actual inline fun testSupportedWindowEvent(name: String): Boolean {
    return false
}
