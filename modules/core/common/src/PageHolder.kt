@file:Suppress("NOTHING_TO_INLINE")

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
interface PageHolder<R : PageHolder<R>> : SerialPageHolder, PageHolderFactory<R> {
    fun init(ctx: PageContext)

    @Suppress("UNCHECKED_CAST")
    override fun create(ctx: PageContext): R {
        return (this as R).also { it.init(ctx) }
    }
}
