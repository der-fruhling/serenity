package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.key

actual inline val isClient: Boolean
    @Composable
    @ReadOnlyComposable
    @OptIn(InternalPageEntryPoint::class)
    inline get() = isClientLocal.current

@PublishedApi
@InternalPageEntryPoint
internal val isClientLocal = compositionLocalOf { true }

@Composable
actual fun ifClient(fn: @Composable (() -> Unit)) {
    if(isClient) {
        fn()
    }
}

@Composable
actual fun <T> alternative(
    onServer: @Composable (() -> T),
    onClient: @Composable (() -> T)
) {
    if(isClient) {
        onClient()
    } else {
        onServer()
    }
}
