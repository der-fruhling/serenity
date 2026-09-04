package net.derfruhling.serenity.style

import net.derfruhling.serenity.platform.Apply

interface StylistTarget<T> : Apply<T, Nothing> {
    override val parent: Nothing?
        get() = null

    fun start() {}
    fun end() {}
}
