package net.derfruhling.serenity.gradle.stylist

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property

abstract class SerenityStylistExtension : ExtensionAware {
    abstract val prettyCssInProduction: Property<Boolean>
    abstract val sourceMapsInProduction: Property<Boolean>

    init {
        prettyCssInProduction.convention(false)
        sourceMapsInProduction.convention(false)
    }
}