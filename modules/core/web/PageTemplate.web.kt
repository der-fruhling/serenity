package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import io.github.oshai.kotlinlogging.KotlinLogging
import web.storage.localStorage

private fun createSaveableStateRegistry(page: PageHolder<*>): SaveableStateRegistry {
    val logger = KotlinLogging.logger {}
    val key = keyOf(page)
    return SaveableStateRegistry(localStorage.getItem(key)?.let {
        try {
            SerialRegistry.decode<SerialSavedData>(it).items()
        } catch (e: Exception) {
            logger.warn(e) { "An error occurred loading save data for page $key" }
            null
        }
    }) { true }
}

private fun keyOf(page: PageHolder<*>): String = "saved-" + page.id

actual class SaveDataManager(
    private val page: PageHolder<*>,
    private val base: SaveableStateRegistry
) :
    SaveableStateRegistry by base, RememberObserver {
    actual constructor(page: PageHolder<*>) : this(page, createSaveableStateRegistry(page))

    actual fun save() {
        localStorage.setItem(keyOf(page), SerialRegistry.encode(SerialSavedData.of(performSave())))
    }

    override fun onAbandoned() {}

    override fun onForgotten() {
        compositionCompletionHandler = null
    }

    override fun onRemembered() {
        compositionCompletionHandler = this::save
    }

    @Composable
    actual fun enter(fn: @Composable (() -> Unit)) {
        CompositionLocalProvider(LocalSaveableStateRegistry provides this) {
            fn()
        }
    }
}