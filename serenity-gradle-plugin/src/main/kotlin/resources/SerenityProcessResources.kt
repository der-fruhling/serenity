package net.derfruhling.serenity.gradle.resources

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.Sync
import java.io.File
import javax.inject.Inject

abstract class SerenityProcessResources : Sync() {
    @get:ServiceReference
    abstract val sourceMapFixer: Property<SourceMapFixerService>

    @get:Input
    abstract val sourceRoots: ListProperty<String>

    @get:Inject
    abstract val objects: ObjectFactory

    init {
        val projectDir = project.layout.projectDirectory
        doLast {
            sourceMapFixer.get().fixSourceMaps(
                sourceRoots.get().map { projectDir.file(it).asFile },
                destinationDir,
                objects.fileTree().from(destinationDir)
            )
        }
    }
}
