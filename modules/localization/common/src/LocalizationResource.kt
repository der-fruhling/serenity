package net.derfruhling.serenity.localization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName($$"$localization-resource")
class LocalizationResource(
    val tag: LanguageTag,
    val strings: Map<Long, String>
) {
}
