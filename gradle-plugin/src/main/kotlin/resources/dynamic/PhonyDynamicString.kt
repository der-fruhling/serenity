package net.derfruhling.serenity.gradle.resources.dynamic

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName($$"$dyn-str")
@JvmInline
value class PhonyDynamicString(val component: PhonyDynamicStringComponent) {

}
