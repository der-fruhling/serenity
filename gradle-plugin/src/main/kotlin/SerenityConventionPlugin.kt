package net.derfruhling.serenity.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class SerenityConventionPlugin : Plugin<Project> {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    override fun apply(target: Project) {
        target.afterEvaluate {
            configure<KotlinMultiplatformExtension> {
                val projectDirectory = target.layout.projectDirectory
                sourceSets.configureEach {
                    when (name) {
                        "jvmMain" -> {
                            kotlin.setSrcDirs(listOf(projectDirectory.dir("jvm/src")))
                            resources.setSrcDirs(listOf(projectDirectory.dir("jvm/resources")))
                        }
                        "jvmTest" -> {
                            kotlin.setSrcDirs(listOf(projectDirectory.dir("test/jvm/src")))
                            resources.setSrcDirs(listOf(projectDirectory.dir("test/jvm/resources")))
                        }
                        "commonMain" -> {
                            kotlin.setSrcDirs(listOf(projectDirectory.dir("common/src")))
                            resources.setSrcDirs(listOf(projectDirectory.dir("common/resources")))
                        }
                        "commonTest" -> {
                            kotlin.setSrcDirs(listOf(projectDirectory.dir("test/common")))
                            resources.setSrcDirs(emptyList<Directory>())
                        }
                        "serverMain" -> {
                            kotlin.setSrcDirs(listOf(projectDirectory.dir("server/src")))
                            resources.setSrcDirs(listOf(projectDirectory.dir("server/resources")))
                        }
                        "serverTest" -> {
                            kotlin.setSrcDirs(listOf(projectDirectory.dir("test/server")))
                            resources.setSrcDirs(emptyList<Directory>())
                        }
                        "nativeMain" -> {
                            kotlin.setSrcDirs(listOf(projectDirectory.dir("native/src")))
                            resources.setSrcDirs(listOf(projectDirectory.dir("native/resources")))
                        }
                        "webMain" -> {
                            kotlin.setSrcDirs(listOf(projectDirectory.dir("web")))
                            resources.setSrcDirs(emptyList<Directory>())
                        }
                        "webTest" -> {
                            kotlin.setSrcDirs(listOf(projectDirectory.dir("test/web")))
                            resources.setSrcDirs(emptyList<Directory>())
                        }
                        else -> {
                            if(name.endsWith("Test")) {
                                kotlin.setSrcDirs(emptyList<Directory>())
                            } else if (name.startsWith("linux") || name.startsWith("macos")) {
                                kotlin.setSrcDirs(listOf(projectDirectory.dir("native-platform").dir(name.replace("Main", ""))))
                            } else {
                                kotlin.setSrcDirs(listOf(projectDirectory.dir(name.replace("Main", ""))))
                            }

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
                                it.generatedKotlin.srcDir(sources)
                            }
                        }
                    }
                }
            }
        }
    }
}
