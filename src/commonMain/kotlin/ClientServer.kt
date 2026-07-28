@file:HtmlComposable

package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.key
import net.derfruhling.serenity.annotations.HtmlComposable

@get:Composable
@get:ReadOnlyComposable
expect inline val isClient: Boolean

inline val isServer: Boolean
    @Composable
    @ReadOnlyComposable
    inline get() = !isClient

@Composable
expect fun ifClient(fn: @Composable () -> Unit)

@Composable
fun ifServer(fn: @Composable () -> Unit) {
    key(isServer) {
        if(isServer) {
            fn()
        }
    }
}

@Composable
expect fun <T> alternative(onServer: @Composable () -> T, onClient: @Composable () -> T)
