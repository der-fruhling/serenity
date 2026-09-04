package net.derfruhling.serenity.localization

import net.derfruhling.serenity.SerialRegistry
import net.derfruhling.serenity.modularity.Module

object LocalizationModule : Module("serenity-localization") {
    override fun initialize() {
        SerialRegistry.registerManifestEntry<AvailableLocalizations>()
    }
}
