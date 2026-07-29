package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import net.derfruhling.serenity.elements.HeadContext
import kotlin.reflect.KClass

abstract class PageRegistry {
    abstract fun head(fn: @Composable HeadContext.() -> Unit)

    abstract fun <T: PageHolder> register(kClass: KClass<T>, kSerializer: KSerializer<T>, page: T)

    inline fun <reified T: PageHolder> register(page: T) {
        register(T::class, serializer<T>(), page)
    }
}
