package net.derfruhling.serenity

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.savedState

internal object SerialMapping {
    fun toSerial(value: Any?): Any? {
        return when (value) {
            is SavedState -> value.read { SerialSavedState.of(toMap()) }
            is List<*> -> mapList(value, ::toSerial)
            is Array<*> -> mapList(value.toList(), ::toSerial).toTypedArray()
            is MutableState<*> -> MutableStateWrapper(toSerial(value.value))
            else -> value
        }
    }

    fun fromSerial(value: Any?): Any? {
        return when (value) {
            is SerialSavedState -> savedState(value.items.mapValues { (_, value) -> fromSerial(value) })
            is List<*> -> mapList(value, ::fromSerial)
            is Array<*> -> mapList(value.toList(), ::fromSerial).toTypedArray()
            is MutableStateWrapper<*> -> mutableStateOf(fromSerial(value.state))
            else -> value
        }
    }

    fun mapList(list: List<Any?>, mapFn: (Any?) -> Any?): List<Any?> {
        return list.map { mapFn(it) }
    }
}
