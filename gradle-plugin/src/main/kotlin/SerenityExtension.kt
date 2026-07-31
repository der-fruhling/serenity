package net.derfruhling.serenity.gradle

import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.internal.artifacts.dependencies.DefaultMutableVersionConstraint
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import javax.inject.Inject

@SerenityGradleDsl
abstract class SerenityExtension(internal val mpp: KotlinMultiplatformExtension) : ExtensionAware {
    abstract val javaVersion: Property<Int>
    abstract val composeHtmlVersion: Property<String>
    abstract val disableDefaultDependencies: Property<Boolean>
    abstract val serverTargets: DomainObjectSet<String>
    abstract val webTargets: DomainObjectSet<String>

    @get:Inject
    abstract val project: Project

    abstract val serenityVersion: Property<VersionConstraint>

    init {
        javaVersion.convention(25)
        composeHtmlVersion.convention("0.1.0-SNAPSHOT")
        disableDefaultDependencies.convention(false)

        val defaultSerenityVersion by lazy {
            project.extensions.findByType<VersionCatalog>()?.let { c ->
                c.findVersion("serenity").orElse(null)?.let { return@lazy it }
            }

            val specVersion = this.javaClass.getResource("VERSION")!!.readText()
            DefaultMutableVersionConstraint(specVersion)
        }

        serenityVersion.convention(project.provider { defaultSerenityVersion })
        extensions.create("collectors", SerenityCollectorsExtension::class, this)
    }

    fun disableDefaultDependencies() {
        disableDefaultDependencies.set(true)
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
}
