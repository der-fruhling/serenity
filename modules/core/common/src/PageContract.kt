package net.derfruhling.serenity

import androidx.compose.runtime.DisallowComposableCalls
import net.derfruhling.serenity.annotations.Intrinsic
import net.derfruhling.serenity.annotations.PageExtensionImplementationApi
import net.derfruhling.serenity.modularity.extension.AbstractPageExtension
import net.derfruhling.serenity.modularity.extension.AbstractPageExtensionVoid
import kotlin.reflect.KClass

interface PageContract {
    var title: String

    @PageExtensionImplementationApi
    fun <T : AbstractPageExtension<*>> extend(kClass: KClass<T>, value: T)
}

inline fun <reified T : AbstractPageExtensionVoid> PageContract.extend(value: T) {
    @OptIn(PageExtensionImplementationApi::class)
    extend(T::class, value)
}

inline fun <reified T : AbstractPageExtension<C>, C> PageContract.extend(value: T, noinline cfg: C.() -> Unit) {
    @OptIn(PageExtensionImplementationApi::class)
    extend(T::class, value.also { it.configure(cfg) })
}

inline fun <reified T : AbstractPageExtensionVoid> PageContract.extend(value: () -> T) {
    @OptIn(PageExtensionImplementationApi::class)
    extend(T::class, value())
}

inline fun <reified T : AbstractPageExtension<C>, C> PageContract.extend(value: () -> T, noinline cfg: C.() -> Unit) {
    @OptIn(PageExtensionImplementationApi::class)
    extend(T::class, value().also { it.configure(cfg) })
}

@Intrinsic
fun pageContract(@Suppress("unused") fn: @DisallowComposableCalls PageContract.() -> Unit) {}
