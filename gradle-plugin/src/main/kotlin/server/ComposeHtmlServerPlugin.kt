package net.derfruhling.html.gradle.server

import net.derfruhling.html.gradle.ComposeHtmlBasePlugin
import net.derfruhling.html.gradle.web.ComposeHtmlWebPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ComposeHtmlServerPlugin : Plugin<Project> {
    lateinit var serverExtension: ComposeHtmlServerExtension
        private set

    override fun apply(target: Project) {
        val base = target.plugins.apply(ComposeHtmlBasePlugin::class)
        serverExtension = base.extension.extensions.create("server", ComposeHtmlServerExtension::class, base.extension)

        val buildDir = target.layout.buildDirectory

        val processServerResources = target.tasks.register("processServerResources", ProcessResources::class) {
            into(target.layout.buildDirectory.dir("resources/server"))

            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }

        val processServerResourcesDebug = target.tasks.register("processServerResourcesDebug", ProcessResources::class) {
            into(target.layout.buildDirectory.dir("resources/server"))

            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }

        target.afterEvaluate {
            target.configure<KotlinMultiplatformExtension> {
                val serverResources = sourceSets.named("serverMain").map { it.resources }

                processServerResources.configure {
                    from(serverResources)
                }
            }
        }

        target.plugins.withType(ComposeHtmlWebPlugin::class) {

            target.afterEvaluate {
                if(base.extension.mpp.targets.any { it.name == "jvm" }) {
                    target.tasks.named("jvmProcessResources", ProcessResources::class) {
                        dependsOn(processServerResources)
                        exclude("static")
                        inputs.dir(buildDir.dir("resources/server"))

                        into("_static") {
                            from(target.tasks.named("processCommonResources"), processServerResources)
                        }
                    }
                }

                val outDir = buildDir.dir("builtNatives").get()
                fun setupVariant(jsVariant: String, processServerResources: TaskProvider<ProcessResources>) {
                    val jsVariantCap = jsVariant[0].uppercase() + jsVariant.substring(1)

                    val tasks = webExtension.jsCompilations.get()
                        .map {
                            "${it}Browser${jsVariantCap}Webpack" to buildDir
                                .dir("kotlin-webpack/$it/${jsVariant}Executable")
                        }

                    val buildWebStuff = target.tasks.register("buildWebStuff${jsVariantCap}", Sync::class) {
                        dependsOn(tasks.map { (name, _) -> name })
                        into(outDir)

                        outputs.dir(outDir)

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
