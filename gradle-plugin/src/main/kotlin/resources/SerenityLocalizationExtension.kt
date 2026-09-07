package net.derfruhling.serenity.gradle.resources

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.ExtensionAware

abstract class SerenityLocalizationExtension : ExtensionAware {
    abstract val sourceDir: DirectoryProperty
}
