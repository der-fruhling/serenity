package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.derfruhling.serenity.modularity.extension.AbstractPageExtension
import kotlin.reflect.KClass

@Immutable
@Polymorphic
// Kotlin/Wasm
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(PolymorphicSerializer::class)
interface SerialPageHolder {
    val hash: Map<String, String>
        get() = emptyMap()
    val details: PageDetails

    @Transient
    val extensions: Map<KClass<out Annotation>, AbstractPageExtension>
        get() = emptyMap()

    @Composable
    @HtmlComposable
    fun Main()
}
