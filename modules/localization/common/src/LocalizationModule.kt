package net.derfruhling.serenity.localization

import net.derfruhling.serenity.modularity.ProvideContext
import net.derfruhling.serenity.modularity.SimpleModule
import net.derfruhling.serenity.serial.SerialRegistry

object LocalizationModule : SimpleModule("serenity-localization") {
    override fun initialize() {
        SerialRegistry.registerManifestEntry<AvailableLocalizations>()
    }

    override suspend fun ProvideContext.provide() {
        actualProvide()
    }
}

internal expect suspend fun ProvideContext.actualProvide()
