package net.derfruhling.html.gradle.resources

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class ComposeApplicationManifest : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFragments: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputManifest: RegularFileProperty

    @get:Input
    abstract val prettyJson: Property<Boolean>

    @get:ServiceReference
    abstract val vendorService: Property<ResourceVendorService>

    @TaskAction
    fun generate() {
        val json = vendorService.get().createJson(prettyJson.get())
        val fragments = sourceFragments.map { json.decodeFromString<ManifestEntry>(it.readText()) }
        outputManifest.asFile.get().writeText(json.encodeToString(fragments))
    }
}
