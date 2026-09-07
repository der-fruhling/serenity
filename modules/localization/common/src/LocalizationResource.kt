package net.derfruhling.serenity.localization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.CborArray
import net.derfruhling.serenity.dynamic.DynamicString

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@SerialName($$"$localization-resource")
@CborArray
class LocalizationResource(
    val tag: LanguageTag,
    val strings: Map<ConstantName, DynamicString>
)
