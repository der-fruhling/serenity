@file:Suppress("UnstableApiUsage")

package net.derfruhling.serenity.gradle

import com.google.devtools.ksp.gradle.KspGradleSubplugin
import net.derfruhling.serenity.gradle.resources.SerenityProcessResources
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradleSubplugin
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlinx.serialization.gradle.SerializationGradleSubplugin

class SerenityBasePlugin : Plugin<Project> {
    lateinit var extension: SerenityExtension
        private set

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    override fun apply(target: Project) {
        target.plugins.apply(KotlinMultiplatformPluginWrapper::class)
        target.plugins.apply(SerializationGradleSubplugin::class)
        target.plugins.apply(ComposeCompilerGradleSubplugin::class)

        target.repositories.mavenCentral()
        target.repositories.google()

        val mppExtension = target.extensions.getByType(KotlinMultiplatformExtension::class)
        extension = target.extensions.create("serenity", SerenityExtension::class, mppExtension)

        target.configure<KotlinMultiplatformExtension> {
            applyHierarchyTemplate {
                common {
                    group("server") {
                        withJvm()

                        group("native") {
                            group("linux") {
                                withLinuxArm64()
                                withLinuxX64()
                            }

                            group("macos") {
                                withMacosArm64()
                            }
                        }
                    }

                    group("web") {
                        withJs()
                        withWasmJs()
                    }
                }
            }

            compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }

            val commonResources = sourceSets.commonMain.map { it.resources }
            val projectDir = project.layout.projectDirectory

            target.tasks.register("processCommonResources", SerenityProcessResources::class) {
                into(target.layout.buildDirectory.dir("resources/common"))
                from(commonResources)

                sourceRoots.set(commonResources.map { it.srcDirs.map { f -> f.toRelativeString(projectDir.asFile) } })

                duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }

            target.tasks.register("processCommonResourcesDebug", SerenityProcessResources::class) {
                into(target.layout.buildDirectory.dir("resources/common-debug"))
                from(commonResources)

                sourceRoots.set(commonResources.map { it.srcDirs.map { f -> f.toRelativeString(projectDir.asFile) } })

                duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }
        }
    }

}
