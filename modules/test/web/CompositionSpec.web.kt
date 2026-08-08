package net.derfruhling.serenity.test

import kotlinx.coroutines.withContext
import net.derfruhling.serenity.AnimationFrameClock

internal actual suspend inline fun <T> withFrameClock(crossinline fn: suspend () -> T): T {
    return withContext(AnimationFrameClock) {
        fn()
    }
}