@file:HtmlComposable

package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

abstract class PageRegistry {
    abstract fun template(fn: @Composable TemplateBuilder.() -> Unit)

    abstract fun <R : PageHolder<R>, T : PageHolderFactory<PageContext, R>> register(
        kClass: KClass<R>,
        kSerializer: KSerializer<R>,
        page: T
    )

    @Suppress("UNCHECKED_CAST")
    inline fun <reified R : PageHolder<R>, reified T : PageHolderFactory<PageContext, R>> register(
        page: T
    ) = register(
        R::class,
        (page as? PageSerializerProvider<R>
            ?: throw IllegalArgumentException("Provided page or page factory **must** implement PageSerializerProvider, which should be the case if you use the compiler plugin. The provided page instance $page does not implement this interface.")).serializer(),
        page
    )
}
