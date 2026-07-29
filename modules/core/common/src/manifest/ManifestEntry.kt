package net.derfruhling.serenity.manifest

import androidx.compose.runtime.ProvidedValue
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Transient

@Polymorphic
interface ManifestEntry {
    @Transient
    val provide: Array<ProvidedValue<*>>
}
