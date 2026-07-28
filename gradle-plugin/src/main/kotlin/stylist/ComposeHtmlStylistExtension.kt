package net.derfruhling.html.gradle.stylist

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property

abstract class ComposeHtmlStylistExtension : ExtensionAware {
    abstract val prettyCssInProduction: Property<Boolean>
    abstract val sourceMapsInProduction: Property<Boolean>

    init {
        prettyCssInProduction.convention(false)
        sourceMapsInProduction.convention(false)
    }
}