package net.derfruhling.serenity.localization

import net.derfruhling.serenity.SerialRegistry
import net.derfruhling.serenity.modularity.Module
import net.derfruhling.serenity.modularity.ProvideContext

object LocalizationModule : Module("serenity-localization") {
    override fun initialize() {
        SerialRegistry.registerManifestEntry<AvailableLocalizations>()
    }

    override suspend fun ProvideContext.provide() {
        actualProvide()
    }
}

internal expect suspend fun ProvideContext.actualProvide()
