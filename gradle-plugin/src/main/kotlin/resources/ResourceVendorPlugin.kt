package net.derfruhling.html.gradle.resources

import net.derfruhling.html.gradle.ComposeHtmlBasePlugin
import net.derfruhling.html.gradle.server.ComposeHtmlServerExtension
import net.derfruhling.html.gradle.server.ComposeHtmlServerPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.kotlin.dsl.withType

class ResourceVendorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val base = target.plugins.apply(ComposeHtmlBasePlugin::class)
        target.gradle.sharedServices.registerIfAbsent("resourceVendor", ResourceVendorService::class)

        val ext = base.extension.extensions.create("resources", ComposeHtmlResourcesExtension::class)

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

        target.plugins.withType(ComposeHtmlServerPlugin::class) {
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