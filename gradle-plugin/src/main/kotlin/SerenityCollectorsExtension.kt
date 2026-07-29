@file:Suppress("UnstableApiUsage")

package net.derfruhling.serenity.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.MutableVersionConstraint
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.DependencyFactory
import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.gradle.api.internal.artifacts.dependencies.DefaultMinimalDependency
import org.gradle.internal.extensions.stdlib.capitalized
import javax.inject.Inject

@SerenityGradleDsl
abstract class SerenityCollectorsExtension(val base: SerenityExtension) {
    @get:Inject
    abstract val project: Project

    @get:Inject
    abstract val dependencyFactory: DependencyFactory

    @JvmOverloads
    fun project(name: String, configuration: String? = null): ProjectDependency {
        return dependencyFactory.createProjectDependency(name).also {
            it.targetConfiguration = configuration
        }
    }

    fun useCommon() {
        useCommon(DefaultMinimalDependency(
            DefaultModuleIdentifier.newId("net.derfruhling.serenity", "serenity-common-collector"),
            base.serenityVersion.get() as MutableVersionConstraint
        ))
    }

    fun useCommon(collector: Any) {
        project.dependencies.add("kspCommonMainMetadata", collector)
    }

    fun useServerKtor() {
        useServer(DefaultMinimalDependency(
            DefaultModuleIdentifier.newId("net.derfruhling.serenity", "serenity-ktor-collector"),
            base.serenityVersion.get() as MutableVersionConstraint
        ))
    }

    fun useServer(collector: Any) {
        base.serverTargets.configureEach {
            project.dependencies.add("ksp${this.capitalized()}", collector)
        }
    }

    fun useWeb() {
        useWeb(DefaultMinimalDependency(
            DefaultModuleIdentifier.newId("net.derfruhling.serenity", "serenity-web-collector"),
            base.serenityVersion.get() as MutableVersionConstraint
        ))
    }

    fun useWeb(collector: Any) {
        base.webTargets.configureEach {
            project.dependencies.add("ksp${this.capitalized()}", collector)
        }
    }
}
