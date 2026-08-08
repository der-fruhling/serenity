package net.derfruhling.serenity.gradle.resources

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile

abstract class LocatePageScript : DefaultTask() {
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:ServiceReference("resourceVendor")
    abstract val vendorService: Property<ResourceVendorService>

    @get:Input
    abstract val prettyJson: Property<Boolean>
}
