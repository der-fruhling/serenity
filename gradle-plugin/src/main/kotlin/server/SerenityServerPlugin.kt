package net.derfruhling.serenity.gradle.server

import net.derfruhling.serenity.gradle.SerenityApplicationPlugin
import net.derfruhling.serenity.gradle.SerenityBasePlugin
import net.derfruhling.serenity.gradle.resources.SerenityProcessResources
import net.derfruhling.serenity.gradle.resources.SourceMapFixerService
import net.derfruhling.serenity.gradle.web.SerenityWebPlugin
import net.derfruhling.serenity.gradle.web.TransformWebAssembly
import net.derfruhling.serenity.gradle.web.WebAssemblySourceMapRemapTransformer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import javax.inject.Inject

class SerenityServerPlugin: Plugin<Project> {
    lateinit var serverExtension: SerenityServerExtension
        private set

    override fun apply(target: Project) {
        val base = target.plugins.apply(SerenityBasePlugin::class)
        target.plugins.apply(SerenityApplicationPlugin::class)
        serverExtension = base.extension.extensions.create(
            "server",
            SerenityServerExtension::class,
            base.extension
        )
        SourceMapFixerService.registerIfAbsent(target)

        val buildDir = target.layout.buildDirectory

        val processServerResources =
            target.tasks.register("processServerResources", SerenityProcessResources::class) {
                into(target.layout.buildDirectory.dir("resources/server"))

                duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }

        val processServerResourcesDebug =
            target.tasks.register("processServerResourcesDebug", SerenityProcessResources::class) {
                into(target.layout.buildDirectory.dir("resources/server-debug"))

                duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }

        target.afterEvaluate {
            target.configure<KotlinMultiplatformExtension> {
                val serverResources = sourceSets.named("serverMain").map { it.resources }

                val projectDir = project.layout.projectDirectory
                processServerResources.configure {
                    from(serverResources)

                    sourceRoots.set(serverResources.map {
                        it.srcDirs.map { f ->
                            f.toRelativeString(
                                projectDir.asFile
                            )
                        }
                    })
                }

                processServerResourcesDebug.configure {
                    from(serverResources)

                    sourceRoots.set(serverResources.map {
                        it.srcDirs.map { f ->
                            f.toRelativeString(
                                projectDir.asFile
                            )
                        }
                    })
                }
            }
        }

        target.plugins.withType(SerenityWebPlugin::class) {
            target.afterEvaluate {
                val outDir = buildDir.dir("builtNatives").get()
                fun setupVariant(
                    jsVariant: String,
                    processServerResources: TaskProvider<SerenityProcessResources>
                ) {
                    val jsVariantCap = jsVariant[0].uppercase() + jsVariant.substring(1)

                    val tasks = webExtension.jsCompilations.get()
                        .map {
                            "${it}Browser${jsVariantCap}Webpack" to buildDir
                                .dir("kotlin-webpack/$it/${jsVariant}Executable")
                        }

                    val variantDir = outDir.dir(jsVariant)
                    val buildWebStuff =
                        target.tasks.register("buildWebStuff${jsVariantCap}", Sync::class) {
                            dependsOn(tasks.map { (name, _) -> name })
                            into(variantDir)

                            outputs.dir(variantDir)
                        }

                    tasks.forEach { (taskName, out) ->
                        val task = target.tasks.getByName(taskName, KotlinWebpack::class)
                        when (val name = taskName.takeWhile { it.isLowerCase() }) {
                            "js" -> buildWebStuff.configure {
                                inputs.dir(out)
                                from(out)
                            }

                            else -> {
                                val actualDir = task.outputDirectory

                                val transformDir =
                                    target.layout.buildDirectory.dir("transform/$name/$jsVariant")

                                val transformTask = target.tasks.register(
                                    "transform${taskName.capitalized()}",
                                    TransformWebAssembly::class
                                ) {
                                    group = "build"
                                    description =
                                        "Runs transformations on the $jsVariant WebAssembly binary and source map"

                                    inputs.dir(actualDir)
                                    dependsOn(task)

                                    inputBinaryDir.set(actualDir)
                                    outputDir.set(transformDir)
                                }

                                if (jsVariant == "development") {
                                    val name = project.provider {
                                        val rootProject = project.rootProject
                                        if (project != rootProject) (rootProject.name + project.path).replace(
                                            ':',
                                            '-'
                                        )
                                        else rootProject.name
                                    }

                                    val sourceMapPath =
                                        name.map { project.layout.buildDirectory.file("compileSync/wasmJs/main/developmentExecutable/kotlin/$it.wasm.map") }
                                            .get()

                                    transformTask.configure {
                                        transformers.register(
                                            "remapSourceMap",
                                            WebAssemblySourceMapRemapTransformer::class
                                        ) {
                                            oldSourceMapDir.set(target.layout.dir(sourceMapPath.map { it.asFile.parentFile }))
                                            sourceDirectory.set(target.rootProject.layout.projectDirectory)
                                        }

                                        inputSourceMap.set(sourceMapPath)
                                    }
                                }

                                buildWebStuff.configure {
                                    into(name) {
                                        from(transformTask)

                                        from(out) {
                                            include("*.js", "*.js.map")
                                        }
                                    }
                                }
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
