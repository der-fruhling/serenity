package net.derfruhling.serenity.gradle.resources

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class SerenityResourcesExtension @Inject constructor(project: Project) : ExtensionAware {
    abstract val resourceIndexFile: RegularFileProperty
    abstract val sourceBaseUrl: Property<String>
    abstract val targetBaseUrl: Property<String>
    abstract val prettyJson: Property<Boolean>

    init {
        resourceIndexFile.convention(project.layout.buildDirectory.file("resources/resource-index.json"))
        prettyJson.convention(false)
    }
}
