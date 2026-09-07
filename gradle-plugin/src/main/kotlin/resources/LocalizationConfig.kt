package net.derfruhling.serenity.gradle.resources

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalizationConfig(
    @SerialName("base-language")
    val baseLanguage: String
) : java.io.Serializable {
}