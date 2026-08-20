package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Polymorphic
// Kotlin/Wasm
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(PolymorphicSerializer::class)
interface SerialPageHolder {
    val hash: Map<String, String>
        get() = emptyMap()
    val details: PageDetails

    @Composable
    @HtmlComposable
    fun Main()
}

@Immutable
@Polymorphic
// Kotlin/Wasm
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(PolymorphicSerializer::class)
interface PageHolder<R : PageHolder<R>> : SerialPageHolder, PageHolderFactory<Any?, R> {
    @Suppress("UNCHECKED_CAST")
    override fun create(ctx: Any?): R {
        return this as R
    }
}

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

@Serializable
@SerialName($$"$page-details")
data class PageDetails(val title: String? = null)
