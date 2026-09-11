package net.derfruhling.serenity.channel

import net.derfruhling.serenity.manifest.Manifest

interface WebChannelImpl<C> {
    suspend fun connect(manifest: Manifest): Connection
}

actual interface ChannelImpl<C> : WebChannelImpl<C> {
    actual fun configure(fn: C.() -> Unit)
}
