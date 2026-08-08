package net.derfruhling.serenity.test

internal actual suspend inline fun <T> withFrameClock(crossinline fn: suspend () -> T): T {
    return fn()
}
