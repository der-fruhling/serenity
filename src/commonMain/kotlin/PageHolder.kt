package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import net.derfruhling.serenity.annotations.HtmlComposable
import kotlin.reflect.KClass

@Immutable
@Polymorphic
interface PageHolder {
    val id: String
    val path: String

    @Composable
    @HtmlComposable
    fun Main()
}

interface PageRegistry {
    fun <T: PageHolder> register(kClass: KClass<T>, kSerializer: KSerializer<T>, page: T)
}

inline fun <reified T: PageHolder> PageRegistry.register(page: T) {
    register(T::class, serializer<T>(), page)
}
