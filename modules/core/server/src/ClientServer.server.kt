package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

actual inline val isClient: Boolean
    @Composable
    @ReadOnlyComposable
    inline get() = false

// does nothing
// the hope is that this is enough to get the compiler to eliminate this code block
@Composable
actual inline fun ifClient(fn: @Composable (() -> Unit)) {
}

@Composable
actual inline fun <T> alternative(
    onServer: @Composable (() -> T),
    onClient: @Composable (() -> T)
) {
    onServer()
}