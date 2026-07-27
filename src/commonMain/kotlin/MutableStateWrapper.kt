package net.derfruhling.html

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName($$"$mutable")
data class MutableStateWrapper<T>(val state: T)
