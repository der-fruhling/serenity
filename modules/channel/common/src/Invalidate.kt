package net.derfruhling.serenity.channel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.IntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import net.derfruhling.serenity.HtmlComposable

@Composable
@HtmlComposable
expect fun InvalidateRemotely(key: String, fn: @Composable @HtmlComposable () -> Unit)

@Composable
@HtmlComposable
expect fun countRemoteInvalidations(key: String): IntState

@Composable
@HtmlComposable
inline fun <T> rememberInvalidating(key: String, crossinline fn: @DisallowComposableCalls () -> T): T {
    val invalidations by countRemoteInvalidations(key)
    val value = remember(invalidations, fn)

    return value
}
