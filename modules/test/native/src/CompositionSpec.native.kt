package net.derfruhling.serenity.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope

internal actual suspend inline fun <T> withFrameClock(crossinline fn: suspend CoroutineScope.() -> T): T {
    return coroutineScope { fn() }
}
