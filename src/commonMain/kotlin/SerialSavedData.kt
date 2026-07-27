package net.derfruhling.html

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName($$"$data")
class SerialSavedData private constructor(
    internal val items: Map<String, List<@Polymorphic Any?>>
) {
    val isEmpty: Boolean get() = items.isEmpty()

    fun items() = items.mapValues { (_, value) -> mapList(value, ::fromSerial) }

    override fun toString(): String {
        return "SerialSavedData(items=$items)"
    }

    companion object {
        val empty = SerialSavedData(emptyMap())

        fun of(value: Map<String, List<Any?>>) = SerialSavedData(
            value.mapValues { (_, value) -> mapList(value, ::toSerial) }
        )
    }
}
