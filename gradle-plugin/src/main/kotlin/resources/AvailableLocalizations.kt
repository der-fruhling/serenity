package net.derfruhling.serenity.gradle.resources

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName($$"$localizations")
data class AvailableLocalizations(
    val defaultLanguage: String,
    val languages: Map<String, String>
) : ManifestEntry
