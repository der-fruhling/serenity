package net.derfruhling.serenity.channel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.IntState
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import net.derfruhling.serenity.HtmlComposable

@Composable
@HtmlComposable
@NonRestartableComposable
actual inline fun InvalidateRemotely(key: String, fn: @Composable @HtmlComposable (() -> Unit)) {
    fn()
}

@Suppress("NOTHING_TO_INLINE")
@Composable
@HtmlComposable
@NonRestartableComposable
actual inline fun countRemoteInvalidations(key: String): IntState {
    return object : IntState {
        override val intValue: Int
            get() = 0
    }
}
