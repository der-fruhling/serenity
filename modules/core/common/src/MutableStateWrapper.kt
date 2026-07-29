package net.derfruhling.serenity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName($$"$mutable")
data class MutableStateWrapper<T>(val state: T)
