package net.derfruhling.serenity.gradle.resources

import net.derfruhling.serenity.gradle.*
import net.derfruhling.serenity.gradle.server.SerenityServerPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.attributes.Usage
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.internal.model.NamedObjectInstantiator
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import javax.inject.Inject

@Suppress("UnstableApiUsage")
class SerenityResourcesPlugin @Inject constructor(val objects: NamedObjectInstantiator) : Plugin<Project> {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    override fun apply(target: Project) {
        val base = target.plugins.apply(SerenityBasePlugin::class)
        target.gradle.sharedServices.registerIfAbsent(
            "resourceVendor",
            ResourceVendorService::class
        )
        SourceMapFixerService.registerIfAbsent(target)

        val ext = base.extension.extensions.create("resources", SerenityResourcesExtension::class)

        val resources = target.configurations.create("resources") {
            isCanBeConsumed = false
            isCanBeResolved = false
        }

        val resourcesDebug =
            createResourcesConfiguration(resources, target, "resourcesDebug", Status.DEBUG)
        val resourcesProduction =
            createResourcesConfiguration(
                resources,
                target,
                "resourcesProduction",
                Status.PRODUCTION
            )

        target.dependencies.attributesSchema {
            attribute(Attributes.USAGE) {
                ordered(SerenityUsage::compareTo)
            }

            attribute(Attributes.STATUS) {
                ordered(Status::compareTo)
            }
        }

        val resourceElements =
            createResourceElementsConfiguration(target, "resourceElements", Status.PRODUCTION)
        val resourceElementsDebug =
            createResourceElementsConfiguration(target, "resourceElementsDebug", Status.DEBUG)

        createUnpackTask(target, "unpackResourcesDebug", "unpacked-debug", resourcesDebug)
        createUnpackTask(target, "unpackResources", "unpacked", resourcesProduction)

        val processCommonResources =
            target.tasks.named("processCommonResources", SerenityProcessResources::class)
        val processCommonResourcesDebug =
            target.tasks.named("processCommonResourcesDebug", SerenityProcessResources::class)

        target.configure<KotlinMultiplatformExtension> {
            publishing {
                adhocSoftwareComponent {
                    addVariantsFromConfiguration(resourceElements) {
                        if (this.configurationVariant.attributes.getAttribute(Attributes.USAGE) != SerenityUsage.RESOURCES_ZIP) {
                            skip()
                        } else {
                            mapToMavenScope("runtime")
                            mapToOptional()
                        }
                    }

                    addVariantsFromConfiguration(resourceElementsDebug) {
                        if (this.configurationVariant.attributes.getAttribute(Attributes.USAGE) != SerenityUsage.RESOURCES_ZIP) {
                            skip()
                        } else {
                            mapToMavenScope("runtime")
                            mapToOptional()
                        }
                    }
                }
            }
        }

        val resourcesSync = target.tasks.register("resourcesSync", Sync::class) {
            description = "Syncs resource library in release-mode"
            group = "dist"

            into(target.layout.buildDirectory.dir("distributions/sync"))

            dependsOn(processCommonResources)
            into("${target.group}/${project.name}") {
                from(processCommonResources)
            }
        }

        val resourcesSyncDebug = target.tasks.register("resourcesSyncDebug", Sync::class) {
            description = "Syncs resource library in debug-mode"
            group = "dist"

            into(target.layout.buildDirectory.dir("distributions/sync-debug"))

            dependsOn(processCommonResourcesDebug)
            into("${target.group}/${project.name}") {
                from(processCommonResourcesDebug)
            }
        }

        val resourcesZip = target.tasks.register("resourcesZip", Zip::class) {
            description = "Packages release resources into a .zip file."
            group = "dist"

            dependsOn(processCommonResources)
            archiveClassifier.set("resources")
            into("${target.group}/${archiveBaseName.get()}") {
                from(processCommonResources)
            }
        }

        val resourcesDebugZip = target.tasks.register("resourcesDebugZip", Zip::class) {
            description = "Packages debug resources into a .zip file."
            group = "dist"

            dependsOn(processCommonResourcesDebug)
            archiveClassifier.set("debug-resources")
            into("${target.group}/${archiveBaseName.get()}") {
                from(processCommonResourcesDebug)
            }
        }

        target.artifacts.add("resourceElements", resourcesZip) {
            type = "resources"
            builtBy(resourcesZip)
        }

        resourceElements.outgoing.variants.register("resourceDir") {
            artifact(target.layout.buildDirectory.dir("distributions/sync")) {
                type = "resources"
                builtBy(resourcesSync)
            }

            attributes {
                attribute(Attributes.USAGE, SerenityUsage.RESOURCES_DIR)
            }
        }

        target.artifacts.add("resourceElementsDebug", resourcesDebugZip) {
            type = "debug-resources"
            builtBy(resourcesDebugZip)
        }

        resourceElements.outgoing.variants.register("resourceDirDebug") {
            artifact(target.layout.buildDirectory.dir("distributions/sync-debug")) {
                type = "debug-resources"
                builtBy(resourcesSyncDebug)
            }

            attributes {
                attribute(Attributes.USAGE, SerenityUsage.RESOURCES_DIR)
            }
        }

        target.plugins.withType(SerenityApplicationPlugin::class) {
            val composeApplicationManifest =
                target.tasks.register(
                    "composeApplicationManifest",
                    ComposeApplicationManifest::class
                ) {
                    description =
                        "Builds the final application-manifest.json file for release builds"
                    prettyJson.convention(ext.prettyJson)
                    outputManifest.set(target.layout.buildDirectory.file("resources/release-manifest/application-manifest.json"))
                }

            val composeApplicationManifestDebug =
                target.tasks.register(
                    "composeApplicationManifestDebug",
                    ComposeApplicationManifest::class
                ) {
                    description = "Builds the final application-manifest.json file for debug builds"
                    prettyJson.convention(ext.prettyJson)
                    outputManifest.set(target.layout.buildDirectory.file("resources/debug-manifest/application-manifest.json"))
                }

            target.plugins.withType(SerenityServerPlugin::class) {
                val processServerResources = target.tasks.named("syncServerResources")

                val outDir = target.layout.buildDirectory.dir("resources/vendored")
                val vendorServerResources =
                    target.tasks.register("vendorServerResources", VendorResourcesTask::class) {
                        dependsOn(processServerResources)

                        into(outDir)
                        from(processServerResources)

                        inputs.dir(target.layout.buildDirectory.dir("resources/all"))
                        outputs.dir(outDir)

                        resourceIndexFile.convention(ext.resourceIndexFile)
                        sourceBaseUrl.convention(ext.sourceBaseUrl)
                        targetBaseUrl.convention(ext.targetBaseUrl)
                        prettyJson.convention(ext.prettyJson)
                    }

                composeApplicationManifest.configure {
                    dependsOn(vendorServerResources)
                    sourceFragments.from(vendorServerResources.get().resourceIndexFile)
                }

                this.serverExtension.apply {
                    resourceTask.set(vendorServerResources.name)
                    composeApplicationManifestTask.set(composeApplicationManifest.name)
                    composeApplicationManifestDebugTask.set(composeApplicationManifestDebug.name)
                }
            }
        }
    }

    private fun createResourceElementsConfiguration(
        target: Project,
        name: String,
        status: Status
    ): Configuration =
        target.configurations.create(name) {
            isCanBeDeclared = false
            isCanBeResolved = false
            isCanBeConsumed = true

            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "serenity"))
                attribute(Attributes.USAGE, SerenityUsage.RESOURCES_ZIP)
                attribute(Attributes.STATUS, status)
            }
        }

    private fun createUnpackTask(
        target: Project,
        name: String,
        dirName: String,
        configuration: Configuration
    ) {
        val task = target.tasks.register(name, Sync::class) {
            val destDir = target.layout.buildDirectory.dir("resources/$dirName")
            outputs.dir(destDir)
            inputs.files(configuration)

            into(destDir)
            dependsOn(configuration)

            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }

        target.afterEvaluate {
            task.configure {
                from(configuration.incoming.artifacts.map {
                    if (it.file.isDirectory) {
                        target.fileTree(it.file)
                    } else {
                        target.zipTree(it.file)
                    }
                })
            }
        }
    }

    private fun createResourcesConfiguration(
        base: Configuration,
        target: Project,
        name: String,
        status: Status
    ): Configuration =
        target.configurations.create(name) {
            isCanBeResolved = true
            isCanBeConsumed = false

            extendsFrom(base)

            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "serenity"))
                attribute(Attributes.USAGE, SerenityUsage.RESOURCES_DIR)
                attribute(Attributes.STATUS, status)
                attribute(
                    ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, when (status) {
                        Status.DEBUG -> "debug-resources"
                        Status.PRODUCTION -> "resources"
                    }
                )
            }
        }
}