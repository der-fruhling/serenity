package net.derfruhling.serenity.channel

import net.derfruhling.serenity.manifest.Manifest

expect object Channels {
    internal suspend fun init(manifest: Manifest)
}
