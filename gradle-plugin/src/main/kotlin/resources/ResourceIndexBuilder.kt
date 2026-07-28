package net.derfruhling.html.gradle.resources

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName($$"$resource-index")
class ResourceIndexBuilder(
    val sourceBaseUrl: String?,
    val targetBaseUrl: String?,
    val contents: Map<String, String>
) : ManifestEntry {

}