package net.derfruhling.serenity

import androidx.compose.runtime.Composable

@Suppress("NOTHING_TO_INLINE")
actual class SaveDataManager actual constructor(page: PageHolder) {
    actual inline fun save() {}

    @Composable
    actual fun enter(fn: @Composable (() -> Unit)) {
    }
}