package net.derfruhling.serenity.channel

import net.derfruhling.serenity.modularity.CommonContext
import net.derfruhling.serenity.modularity.Module

object ChannelModule : Module<ChannelConfig>("serenity-channel") {
    override fun defaultConfig(): ChannelConfig {
        return ChannelConfig()
    }

    override fun initialize() {

    }

    override suspend fun CommonContext.asyncInit() {
        Channels.init(getManifest())
    }
}
