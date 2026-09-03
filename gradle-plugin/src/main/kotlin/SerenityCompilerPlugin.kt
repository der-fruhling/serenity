package net.derfruhling.serenity.gradle

import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class SerenityCompilerPlugin : KotlinCompilerPluginSupportPlugin {
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project

        kotlinCompilation.compileTaskProvider.configure {
            // Run this compiler plugin before Compose plugin.
            compilerOptions.freeCompilerArgs.add("-Xcompiler-plugin-order=net.derfruhling.serenity>androidx.compose.compiler.plugins.kotlin")
        }

        return project.provider { emptyList() }
    }

    override fun getCompilerPluginId(): String {
        return "net.derfruhling.serenity"
    }

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "net.derfruhling.serenity",
        artifactId = "serenity-compiler-plugin",
        version = "0.1.0-SNAPSHOT"
    )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true
}