package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.elements.currentPageLocal

@Suppress("NOTHING_TO_INLINE")
actual inline fun navigate(to: PageHolder<*>) {
}

@Suppress("NOTHING_TO_INLINE")
actual inline fun navigateDirect(to: PageHolder<*>) {
}

actual val currentPage: PageHolder<*>
    @Composable
    get() = currentPageLocal.current
