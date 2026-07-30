package net.derfruhling.serenity.event

import kotlinx.datetime.Month
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import net.derfruhling.serenity.annotations.LimitedAvailability
import net.derfruhling.serenity.annotations.Since
import net.derfruhling.serenity.annotations.UnsupportedOnSafari
import net.derfruhling.serenity.annotations.WidelyAvailable
import net.derfruhling.serenity.tree.platform.ElementNode
import kotlin.js.JsExport

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
