package net.derfruhling.serenity.gradle.resources

import net.derfruhling.serenity.gradle.SerenityBasePlugin
import net.derfruhling.serenity.gradle.SerenityExtension
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class SerenityLocalizationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply<SerenityBasePlugin>()
        target.apply<SerenityResourcesPlugin>()

        val baseExt = target.the<SerenityExtension>()
        val ext = baseExt.extensions.create<SerenityLocalizationExtension>("localization")
        val resExt = baseExt.the<SerenityResourcesExtension>()

        ManifestEntryRegistry.registerEntry<AvailableLocalizations>()

        val parseLocale = target.tasks.register<ParseLocale>("parseLocale") {
            group = "build"
            description = "Parses locale files and generates the relevant manifest fragment"

            sources.from(target.fileTree(ext.sourceDir) {
                include("*.xml")
            })
            outputDir.set(target.layout.buildDirectory.dir("locale"))
            configOutput.set(target.layout.buildDirectory.file("resources/manifest/locale.json"))
            configFile.set(ext.sourceDir.file("localization.toml"))
            prettyJson.set(resExt.prettyJson)

            val sourceDirStr = ext.sourceDir.get().asFile.absolutePath
            sourceDir.set(sourceDirStr)

            for(sourceRoot in target.the<KotlinMultiplatformExtension>().sourceSets) {
                val dir = sourceRoot.resources.srcDirs.find { sourceDirStr.startsWith(it.absolutePath) } ?: continue
                baseDir.set(dir.absolutePath)
            }
        }

        target.tasks.withType<SerenityComposeManifestTask>().configureEach {
            dependsOn(parseLocale)

            sourceFragments.from(parseLocale.map { it.configOutput })
        }

        target.afterEvaluate {
            val sourceDirStr = ext.sourceDir.get().asFile.absolutePath
            val baseDir = target.the<KotlinMultiplatformExtension>().sourceSets.firstNotNullOfOrNull { sourceSet ->
                sourceSet.resources.srcDirs.find { sourceDirStr.startsWith(it.absolutePath) }
            } ?: throw InvalidUserDataException("locale source dir must be under a resource root")

            val outputDir = ext.sourceDir.get().asFile.toRelativeString(baseDir)

            tasks.withType<SerenityProcessResources>().configureEach {
                exclude {
                    it.file.resolveSibling("localization.toml").exists()
                }
            }

            tasks.named<SerenityProcessResources>("processCommonResources").configure {
                dependsOn(parseLocale)

                into(outputDir) {
                    from(target.layout.buildDirectory.dir("locale"))
                }
            }

            tasks.named<SerenityProcessResources>("processCommonResourcesDebug").configure {
                dependsOn(parseLocale)

                into(outputDir) {
                    from(target.layout.buildDirectory.dir("locale"))
                }
            }
        }
    }
}