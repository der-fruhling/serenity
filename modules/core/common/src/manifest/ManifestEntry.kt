package net.derfruhling.serenity.manifest

import androidx.compose.runtime.ProvidedValue
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Polymorphic
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(with = PolymorphicSerializer::class)
interface ManifestEntry {
    @Transient
    val provide: Array<ProvidedValue<*>>
}
