package net.derfruhling.serenity.dynamic

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@SerialName($$"$dyn-str")
@Serializable
@JvmInline
value class DynamicString(val component: DynamicStringComponent) {
}

fun String.toDynamicString(): DynamicString = DynamicString(toComponent())
fun Any?.toDynamicString(): DynamicString = this.toString().toDynamicString()
