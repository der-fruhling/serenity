package net.derfruhling.serenity

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable

@Immutable
@Polymorphic
// Kotlin/Wasm
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(PolymorphicSerializer::class)
interface PageHolderFactory<in Ctx, R : PageHolder<R>> {
    val id: String
    val path: String
    fun create(ctx: Ctx): R
}
