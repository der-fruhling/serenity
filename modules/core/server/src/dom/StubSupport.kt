package net.derfruhling.serenity.dom

import net.derfruhling.serenity.annotations.UsedByGeneratedCode
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class NotImplementedDelegate {
    operator fun <T> provideDelegate(self: Any?, property: KProperty<*>): ReadWriteProperty<Any?, T> {
        return object : ReadWriteProperty<Any?, T> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T {
                throw NotImplementedError()
            }

            override fun setValue(
                thisRef: Any?,
                property: KProperty<*>,
                value: T
            ) {
                throw NotImplementedError()
            }
        }
    }
}

@UsedByGeneratedCode
internal val notImplemented = NotImplementedDelegate()
