package net.derfruhling.serenity.manifest

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName($$"$scripts")
@Immutable
data class ScriptLocation(
    val js: String? = null,
    val wasm: String? = null,
    val wasmBinary: String? = null
) : SharedManifestEntry {
    override val provide: Array<ProvidedValue<*>>
        get() = arrayOf(local provides this)

    companion object {
        val local = staticCompositionLocalOf<ScriptLocation> { throw UnsupportedOperationException() }
    }
}
