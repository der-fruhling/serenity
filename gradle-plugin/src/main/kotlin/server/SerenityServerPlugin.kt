package net.derfruhling.serenity.gradle.server

import net.derfruhling.serenity.gradle.SerenityApplicationPlugin
import net.derfruhling.serenity.gradle.SerenityBasePlugin
import net.derfruhling.serenity.gradle.resources.SerenityProcessResources
import net.derfruhling.serenity.gradle.resources.SourceMapFixerService
import net.derfruhling.serenity.gradle.web.SerenityWebPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class SerenityServerPlugin : Plugin<Project> {
    lateinit var serverExtension: SerenityServerExtension
        private set

    override fun apply(target: Project) {
        val base = target.plugins.apply(SerenityBasePlugin::class)
        target.plugins.apply(SerenityApplicationPlugin::class)
        serverExtension = base.extension.extensions.create("server", SerenityServerExtension::class, base.extension)
        SourceMapFixerService.registerIfAbsent(target)

        val buildDir = target.layout.buildDirectory

        val processServerResources = target.tasks.register("processServerResources", SerenityProcessResources::class) {
            into(target.layout.buildDirectory.dir("resources/server"))

            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }

        val processServerResourcesDebug = target.tasks.register("processServerResourcesDebug", SerenityProcessResources::class) {
            into(target.layout.buildDirectory.dir("resources/server-debug"))

            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }

        target.afterEvaluate {
            target.configure<KotlinMultiplatformExtension> {
                val serverResources = sourceSets.named("serverMain").map { it.resources }

                val projectDir = project.layout.projectDirectory
                processServerResources.configure {
                    from(serverResources)

                    sourceRoots.set(serverResources.map { it.srcDirs.map { f -> f.toRelativeString(projectDir.asFile) } })
                }

                processServerResourcesDebug.configure {
                    from(serverResources)

                    sourceRoots.set(serverResources.map { it.srcDirs.map { f -> f.toRelativeString(projectDir.asFile) } })
                }
            }
        }

        target.plugins.withType(SerenityWebPlugin::class) {
            target.afterEvaluate {
                val outDir = buildDir.dir("builtNatives").get()
                fun setupVariant(jsVariant: String, processServerResources: TaskProvider<SerenityProcessResources>) {
                    val jsVariantCap = jsVariant[0].uppercase() + jsVariant.substring(1)

                    val tasks = webExtension.jsCompilations.get()
                        .map {
                            "${it}Browser${jsVariantCap}Webpack" to buildDir
                                .dir("kotlin-webpack/$it/${jsVariant}Executable")
                        }

                    val buildWebStuff = target.tasks.register("buildWebStuff${jsVariantCap}", Sync::class) {
                        dependsOn(tasks.map { (name, _) -> name })
                        into(outDir.dir(jsVariant))

                        outputs.dir(outDir.dir(jsVariant))

                        tasks.forEach { (name, out) ->
                            inputs.dir(out)
                            when(val name = name.takeWhile { it.isLowerCase() }) {
                                "js" -> from(out)
                                else -> into(name) { from(out) }
                            }

                        }
                    }

                    processServerResources.configure {
                        dependsOn(buildWebStuff)

                        into("js") {
                            from(buildWebStuff)
                        }
                    }
                }

                setupVariant("development", processServerResourcesDebug)
                setupVariant("production", processServerResources)
            }
        }
    }
}
