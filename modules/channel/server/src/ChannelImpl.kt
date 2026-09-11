package net.derfruhling.serenity.channel

import kotlinx.coroutines.CoroutineScope
import net.derfruhling.serenity.modularity.Event

interface ServerChannelImpl<C> {
    val onConnect: Event<suspend (Int, Connection) -> Unit>
    val onDisconnect: Event<suspend (Int, Connection) -> Unit>
    val coroutineScope: CoroutineScope? get() = null

    fun close()
}

actual interface ChannelImpl<C> : ServerChannelImpl<C> {
    actual fun configure(fn: C.() -> Unit)
}
