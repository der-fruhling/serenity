package net.derfruhling.serenity.gradle.stylist

import com.sass_lang.embedded_protocol.OutputStyle
import io.freefair.gradle.plugins.sass.SassBasePlugin
import io.freefair.gradle.plugins.sass.SassCompile
import net.derfruhling.serenity.gradle.SerenityBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class SerenitySassPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val base = target.plugins.apply(SerenityBasePlugin::class)
        @Suppress("UnstableApiUsage")
        target.plugins.apply(SassBasePlugin::class)

        val ext = base.extension.extensions.create("stylist", SerenityStylistExtension::class)

        target.configure<KotlinMultiplatformExtension> {
            sourceSets.configureEach {
                fun configureCompileSass(set: String, dir: String) {
                    val debugCompile = target.tasks.register("compile${set}SassDebug", SassCompile::class) {
                        source(resources.asFileTree)
                        destinationDir.set(target.layout.buildDirectory.dir("sass-out/$dir-debug"))
                        sourceMapEnabled.set(true)
                        outputStyle.set(OutputStyle.EXPANDED)
                    }

                    val releaseCompile = target.tasks.register("compile${set}SassRelease", SassCompile::class) {
                        source(resources.asFileTree)
                        destinationDir.set(target.layout.buildDirectory.dir("sass-out/$dir"))
                        sourceMapEnabled.set(ext.sourceMapsInProduction)
                        outputStyle.set(ext.prettyCssInProduction.map { if(it) OutputStyle.EXPANDED else OutputStyle.COMPRESSED })
                    }

                    target.tasks.named("process${set}ResourcesDebug", ProcessResources::class) {
                        exclude("*.scss")
                        exclude("*.sass")

                        from(debugCompile)
                    }

                    target.tasks.named("process${set}Resources", ProcessResources::class) {
                        exclude("*.scss")
                        exclude("*.sass")

                        from(releaseCompile)
                    }
                }

                when (name) {
                    "commonMain" -> configureCompileSass("Common", "common")
                    "serverMain" -> configureCompileSass("Server", "server")
                }
            }
        }
    }
}
