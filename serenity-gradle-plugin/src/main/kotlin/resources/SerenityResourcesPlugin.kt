package net.derfruhling.serenity.gradle.resources

import net.derfruhling.serenity.gradle.SerenityBasePlugin
import net.derfruhling.serenity.gradle.server.SerenityServerPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.kotlin.dsl.withType

class SerenityResourcesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val base = target.plugins.apply(SerenityBasePlugin::class)
        target.gradle.sharedServices.registerIfAbsent("resourceVendor", ResourceVendorService::class)

        val ext = base.extension.extensions.create("resources", SerenityResourcesExtension::class)

        val composeApplicationManifest = target.tasks.register("composeApplicationManifest", ComposeApplicationManifest::class) {
            description = "Builds the final application-manifest.json file for release builds"
            prettyJson.convention(ext.prettyJson)
            outputManifest.set(target.layout.buildDirectory.file("resources/release-manifest/application-manifest.json"))
        }

        val composeApplicationManifestDebug = target.tasks.register("composeApplicationManifestDebug", ComposeApplicationManifest::class) {
            description = "Builds the final application-manifest.json file for debug builds"
            prettyJson.convention(ext.prettyJson)
            outputManifest.set(target.layout.buildDirectory.file("resources/debug-manifest/application-manifest.json"))
        }

        target.plugins.withType(SerenityServerPlugin::class) {
            val processServerResources = target.tasks.named("syncServerResources")

            val outDir = target.layout.buildDirectory.dir("resources/vendored")
            val vendorServerResources = target.tasks.register("vendorServerResources", VendorResourcesTask::class) {
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