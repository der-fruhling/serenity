package net.derfruhling.html.gradle.server

import net.derfruhling.html.gradle.ComposeHtmlBasePlugin
import net.derfruhling.html.gradle.web.ComposeHtmlWebPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources

class ComposeHtmlServerPlugin : Plugin<Project> {
    lateinit var serverExtension: ComposeHtmlServerExtension
        private set

    override fun apply(target: Project) {
        val base = target.plugins.apply(ComposeHtmlBasePlugin::class)
        serverExtension = base.extension.extensions.create("server", ComposeHtmlServerExtension::class, base.extension)

        val buildDir = target.layout.buildDirectory

        val setupStaticResources = target.tasks.register("setupStaticResources", Sync::class) {
            into(buildDir.dir("staticResources"))

            into("_static/resources") {
                from(target.layout.projectDirectory.dir("resources"))
            }
        }

        target.plugins.withType(ComposeHtmlWebPlugin::class) {
            val jsVariant = target.findProperty("net.derfruhling.compose-html.variant") as String? ?: "development"
            val jsVariantCap = jsVariant[0].uppercase() + jsVariant.substring(1)
            target.afterEvaluate {
                if(base.extension.mpp.targets.any { it.name == "jvm" }) {
                    target.tasks.named("jvmProcessResources", ProcessResources::class) {
                        dependsOn(setupStaticResources)
                        exclude("static")
                        inputs.dir(buildDir.dir("staticResources"))
                        from(buildDir.dir("staticResources"))
                    }
                }

                val outDir = buildDir.dir("builtNatives").get()
                val tasks = webExtension.jsCompilations.get()
                    .map {
                        "${it}Browser${jsVariantCap}Webpack" to buildDir
                            .dir("kotlin-webpack/$it/${jsVariant}Executable")
                    }

                val buildWebStuff = target.tasks.register("buildWebStuff", Sync::class) {
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

                setupStaticResources.configure {
                    dependsOn(buildWebStuff)

                    into("_static/js") {
                        from(buildWebStuff)
                    }
                }
            }
        }
    }
}
