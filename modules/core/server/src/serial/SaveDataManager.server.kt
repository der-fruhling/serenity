package net.derfruhling.serenity.serial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import net.derfruhling.serenity.PageHolder

@Suppress("NOTHING_TO_INLINE")
actual class SaveDataManager actual constructor(page: PageHolder<*>) {
    actual inline fun save() {}

    @Composable
    @NonRestartableComposable
    actual fun enter(fn: @Composable (() -> Unit)) {
        fn()
    }
}
