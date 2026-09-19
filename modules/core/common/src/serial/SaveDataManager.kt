package net.derfruhling.serenity.serial

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.PageHolder

expect class SaveDataManager(page: PageHolder<*>) {
    fun save()

    @Composable
    fun enter(fn: @Composable () -> Unit)
}
