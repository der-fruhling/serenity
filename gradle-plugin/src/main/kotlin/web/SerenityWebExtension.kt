package net.derfruhling.serenity.gradle.web

import net.derfruhling.serenity.gradle.SerenityDependencyHandler
import net.derfruhling.serenity.gradle.SerenityExtension
import net.derfruhling.serenity.gradle.SerenityGradleDsl
import org.gradle.api.Action
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registerBinding
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JsSourceMapEmbedMode
import org.jetbrains.kotlin.gradle.dsl.JsSourceMapNamesPolicy
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBrowserDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackRule
import javax.inject.Inject

@SerenityGradleDsl
abstract class SerenityWebExtension(internal val base: SerenityExtension) : ExtensionAware {
    @get:Inject
    protected abstract val project: Project

    abstract val includeSourceMapsInProductionBuilds: Property<Boolean>

    init {
        includeSourceMapsInProductionBuilds.convention(false)
    }

    abstract val jsCompilations: ListProperty<String>

    fun js(configure: Action<KotlinJsTargetDsl>) {
        base.mpp.js {
            val outFile = project.provider { project.layout.buildDirectory.file("webpack-glue/js.config.js").get() }
            val generateJsWebpackConfig = project.tasks.register("generateJsWebpackConfig") {
                outputs.file(outFile)
                val name = project.provider { project.name }
                doLast {
                    outFile.get().asFile.writeText("process.env.CFG_PUBLIC_PATH = '/_/js/'; process.env.CFG_PROJECT_NAME = '${name.get()}'")
                }
            }
            commonJsOptions {
                webpackTask {
                    dependsOn(generateJsWebpackConfig)
                    inputs.file(outFile)
                    nodeArgs += listOf("--require=${outFile.get().asFile.toRelativeString(npmProjectDir.get())}")
                }
            }
            jsCompilations.add("js")
            configure.execute(this)
        }
    }

    fun js() {
        js {}
    }

    @ExperimentalWasmDsl
    fun wasmJs(configure: Action<KotlinWasmJsTargetDsl>) {
        base.mpp.wasmJs {
            val outFile = project.provider { project.layout.buildDirectory.file("webpack-glue/wasm.config.js").get() }
            val generateWasmJsWebpackConfig = project.tasks.register("generateWasmJsWebpackConfig") {
                outputs.file(outFile)
                val name = project.provider { project.name }
                doLast {
                    outFile.get().asFile.writeText("process.env.CFG_PUBLIC_PATH = '/_/js/wasm/'; process.env.CFG_PROJECT_NAME = '${name.get()}'")
                }
            }

            commonJsOptions {
                webpackTask {
                    dependsOn(generateWasmJsWebpackConfig)
                    inputs.file(outFile)
                    nodeArgs += listOf("--require=${outFile.get().asFile.toRelativeString(npmProjectDir.get())}")
                }
            }

            jsCompilations.add("wasmJs")
            configure.execute(this)
        }
    }

    @ExperimentalWasmDsl
    fun wasmJs() {
        wasmJs {}
    }

    private fun KotlinJsTargetDsl.commonJsOptions(fn: KotlinJsBrowserDsl.() -> Unit = {}) {
        base.webTargets.add(this.name)

        browser {
            fn()

            webpackTask {
                sourceMaps = mode != KotlinWebpackConfig.Mode.PRODUCTION || includeSourceMapsInProductionBuilds.get()
                if(mode == KotlinWebpackConfig.Mode.DEVELOPMENT) devtool = "source-map"
                mainOutputFileName.set("page.[contenthash].js")
            }
        }

        compilerOptions {
            target.set("es2015")

            sourceMap.set(true)
            sourceMapEmbedSources.set(JsSourceMapEmbedMode.SOURCE_MAP_SOURCE_CONTENT_ALWAYS)
            sourceMapNamesPolicy.set(JsSourceMapNamesPolicy.SOURCE_MAP_NAMES_POLICY_FQ_NAMES)
        }

        binaries.executable()
    }

    fun dependencies(fn: SerenityDependencyHandler.() -> Unit) {
        base.mpp.sourceSets.named("webMain") {
            dependencies {
                object : SerenityDependencyHandler,
                         KotlinDependencyHandler by this {}.fn()
            }
        }
    }

    fun testDependencies(fn: SerenityDependencyHandler.() -> Unit) {
        base.mpp.sourceSets.named("webTest") {
            dependencies {
                object : SerenityDependencyHandler,
                         KotlinDependencyHandler by this {}.fn()
            }
        }
    }
}
