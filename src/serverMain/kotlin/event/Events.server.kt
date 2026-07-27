package net.derfruhling.html.event

import kotlinx.datetime.Month
import kotlinx.serialization.Serializable
import net.derfruhling.html.annotations.LimitedAvailability
import net.derfruhling.html.annotations.Since
import net.derfruhling.html.annotations.UnsupportedOnSafari
import net.derfruhling.html.annotations.WidelyAvailable
import net.derfruhling.html.tree.platform.ElementNode

@Serializable
actual sealed interface BuiltinEventType

@Serializable
actual sealed interface BuiltinPointerEvent
