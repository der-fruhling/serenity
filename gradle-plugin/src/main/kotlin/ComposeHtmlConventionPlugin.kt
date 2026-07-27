package net.derfruhling.html.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ComposeHtmlConventionPlugin : Plugin<Project> {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    override fun apply(target: Project) {
        target.afterEvaluate {
            configure<KotlinMultiplatformExtension> {
                val projectDirectory = target.layout.projectDirectory
                val src = projectDirectory.dir("src")
                sourceSets.configureEach {
                    kotlin.setSrcDirs(listOf(src.dir(name.replace("Main", ""))))

                    when {
                        name.startsWith("jvm") -> {
                            resources.setSrcDirs(listOf(src.dir(name.replace("Main", "") + "Resources")))
                        }

                        name == "commonMain" -> {
                            resources.setSrcDirs(listOf(projectDirectory.dir("resources")))
                        }

                        else -> {
                            resources.setSrcDirs(emptyList<Directory>())
                        }
                    }
                }
            }
        }

        target.plugins.withId("com.google.devtools.ksp") {
            target.afterEvaluate {
                configure<KotlinMultiplatformExtension> {
                    val buildDir = target.layout.buildDirectory.get()
                    targets.configureEach {
                        val kspDir = buildDir.dir("generated/ksp").dir(name)

                        compilations.configureEach {
                            compileTaskProvider.configure {
                                dependsOn(name.replace("compile", "ksp"))
                            }
                            kotlinSourceSets.forEach {
                                val sources = kspDir.dir(it.name).dir("kotlin")
                                it.kotlin.srcDir(sources)
                            }
                        }
                    }
                }
            }
        }
    }
}