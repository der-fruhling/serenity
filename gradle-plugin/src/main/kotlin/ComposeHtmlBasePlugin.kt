package net.derfruhling.html.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.internal.model.NamedObjectInstantiator
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.*
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradleSubplugin
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlinx.serialization.gradle.SerializationGradleSubplugin
import javax.inject.Inject

class ComposeHtmlBasePlugin @Inject constructor(val objects: NamedObjectInstantiator) : Plugin<Project> {
    lateinit var extension: ComposeHtmlExtension
        private set

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    override fun apply(target: Project) {
        target.plugins.apply(KotlinMultiplatformPluginWrapper::class)
        target.plugins.apply(SerializationGradleSubplugin::class)
        target.plugins.apply(ComposeCompilerGradleSubplugin::class)

        target.repositories.mavenCentral()
        target.repositories.google()

        val mppExtension = target.extensions.getByType(KotlinMultiplatformExtension::class)
        extension = target.extensions.create("composeHtml", ComposeHtmlExtension::class, mppExtension)

        val resources = target.configurations.create("resources") {
            isCanBeResolved = true
            isCanBeConsumed = false

            resolutionStrategy {
                componentSelection {
                    all {
                        if(this.metadata?.attributes?.getAttribute(Attributes.USAGE) != ComposeHtmlUsage.RESOURCES) {
                            reject("not a resource bundle")
                        }
                    }
                }

                eachDependency {
                    artifactSelection {
                        selectArtifact("resources", "zip", "resources")
                    }
                }
            }
        }

        val resourceElements = target.configurations.create("resourceElements") {
            isCanBeResolved = false
            isCanBeConsumed = true

            outgoing {
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "compose-html"))
                    attribute(Attributes.USAGE, ComposeHtmlUsage.RESOURCES)
                }
            }
        }

        target.tasks.register("unpackResources", Sync::class) {
            into(target.layout.buildDirectory.dir("resources"))
            from(
                target.provider { resources.resolve().map { target.zipTree(it) } }
            )

            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }

        target.configure<KotlinMultiplatformExtension> {
            publishing {
                adhocSoftwareComponent {
                    addVariantsFromConfiguration(resourceElements) {
                        mapToOptional()
                    }
                }
            }

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

            target.tasks.register("processCommonResources", ProcessResources::class) {
                into(target.layout.buildDirectory.dir("resources/common"))
                from(commonResources)

                duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }

            target.tasks.register("processCommonResourcesDebug", ProcessResources::class) {
                into(target.layout.buildDirectory.dir("resources/common-debug"))
                from(commonResources)

                duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }
        }
    }
}
