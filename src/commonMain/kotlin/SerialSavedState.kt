package net.derfruhling.html

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName($$"$state")
class SerialSavedState private constructor(
    internal val items: Map<String, @Polymorphic Any?>
) {
    override fun toString(): String {
        return "SerialSavedState(items=$items)"
    }

    companion object {
        fun of(items: Map<String, Any?>) = SerialSavedState(
            items.mapValues { (_, value) -> toSerial(value) }
        )
    }
}