package net.derfruhling.serenity.event

import kotlinx.datetime.Month
import kotlinx.serialization.Serializable
import net.derfruhling.serenity.annotations.LimitedAvailability
import net.derfruhling.serenity.annotations.Since
import net.derfruhling.serenity.annotations.UnsupportedOnSafari
import net.derfruhling.serenity.annotations.WidelyAvailable
import net.derfruhling.serenity.tree.platform.ElementNode

@Serializable
actual sealed interface BuiltinEventType

@Serializable
actual sealed interface BuiltinPointerEvent : BuiltinEventType
