package net.derfruhling.serenity

import androidx.compose.runtime.Composable

expect class SaveDataManager(page: PageHolder<*>) {
    fun save()

    @Composable
    fun enter(fn: @Composable () -> Unit)
}
