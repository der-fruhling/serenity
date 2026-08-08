package net.derfruhling.serenity.gradle.resources

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName($$"$scripts")
data class ScriptLocation(
    val js: String? = null,
    val wasm: String? = null,
    val wasmBinary: String? = null
) : ManifestEntry
