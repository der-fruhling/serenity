package net.derfruhling.serenity.logging

actual inline fun setupLogging(fn: () -> Unit) {
    fn()
}
