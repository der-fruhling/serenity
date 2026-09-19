package net.derfruhling.serenity.serial

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName($$"$mutable")
internal data class MutableStateWrapper<T>(val state: T)
