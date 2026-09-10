package net.derfruhling.serenity.gradle

import net.derfruhling.serenity.gradle.resources.SerenityComposeManifestTask
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.internal.artifacts.dependencies.DefaultMutableVersionConstraint
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import javax.inject.Inject

@SerenityGradleDsl
abstract class SerenityExtension(internal val mpp: KotlinMultiplatformExtension) : ExtensionAware {
    abstract val javaVersion: Property<Int>
    abstract val serverTargets: DomainObjectSet<String>
    abstract val webTargets: DomainObjectSet<String>

    @get:Inject
    abstract val project: Project

    abstract val serenityVersion: Property<VersionConstraint>

    init {
        javaVersion.convention(25)

        serenityVersion.convention(project.provider {
            project.extensions.findByType<VersionCatalog>()?.let { c ->
                c.findVersion("serenity").orElse(null)?.let { return@provider it }
            }

            DefaultMutableVersionConstraint(BuildConfig.VERSION)
        })

        extensions.create("collectors", SerenityCollectorsExtension::class, this)
    }

    fun dependencies(fn: SerenityDependencyHandler.() -> Unit) {
        mpp.sourceSets.named("commonMain") {
            dependencies {
                object : SerenityDependencyHandler,
                    KotlinDependencyHandler by this {}.fn()
            }
        }
    }

    fun testDependencies(fn: SerenityDependencyHandler.() -> Unit) {
        mpp.sourceSets.named("commonTest") {
            dependencies {
                object : SerenityDependencyHandler,
                    KotlinDependencyHandler by this {}.fn()
            }
        }
    }

    fun manifest(fn: SerenityComposeManifestTask.() -> Unit) {
        project.tasks.withType(SerenityComposeManifestTask::class).configureEach(fn)
    }

    fun manifestDebug(fn: SerenityComposeManifestTask.() -> Unit) {
        project.tasks.withType(SerenityComposeManifestTask::class).configureEach {
            if (this.name.endsWith("Debug")) fn()
        }
    }
}
