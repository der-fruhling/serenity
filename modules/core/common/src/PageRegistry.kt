@file:HtmlComposable

package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

abstract class PageRegistry<Ctx> {
    abstract fun template(fn: @Composable TemplateBuilder.() -> Unit)

    abstract fun <R : PageHolder<R>, T : PageHolderFactory<Ctx, R>> register(
        kClass: KClass<R>,
        kSerializer: KSerializer<R>,
        page: T
    )

    inline fun <reified R : PageHolder<R>, reified T : PageHolderFactory<Ctx, R>> register(
        page: T
    ) = register(R::class, serializer<R>(), page)
}
