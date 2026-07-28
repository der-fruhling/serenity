package net.derfruhling.html.gradle.web

import net.derfruhling.html.gradle.ComposeDependencyHandler
import net.derfruhling.html.gradle.ComposeHtmlDsl
import net.derfruhling.html.gradle.ComposeHtmlExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import javax.inject.Inject

@ComposeHtmlDsl
abstract class ComposeHtmlWebExtension(internal val base: ComposeHtmlExtension) : ExtensionAware {
    @get:Inject
    abstract val project: Project

    abstract val includeSourceMapsInProductionBuilds: Property<Boolean>

    init {
        includeSourceMapsInProductionBuilds.convention(false)
    }

    abstract val jsCompilations: ListProperty<String>

    fun js(configure: Action<KotlinJsTargetDsl>) {
        base.mpp.js {
            commonJsOptions()
            jsCompilations.add("js")
            configure.execute(this)
        }
    }

    fun js() {
        base.mpp.js {
            commonJsOptions()
            jsCompilations.add("js")
        }
    }

    @ExperimentalWasmDsl
    fun wasmJs(configure: Action<KotlinWasmJsTargetDsl>) {
        base.mpp.wasmJs {
            commonJsOptions()
            jsCompilations.add("wasmJs")
            configure.execute(this)
        }
    }

    @ExperimentalWasmDsl
    fun wasmJs() {
        base.mpp.wasmJs {
            commonJsOptions()
            jsCompilations.add("wasmJs")
        }
    }

    private fun KotlinJsTargetDsl.commonJsOptions() {
        browser {
            webpackTask {
                if(mode == KotlinWebpackConfig.Mode.PRODUCTION) {
                    sourceMaps = includeSourceMapsInProductionBuilds.get()
                }
                mainOutputFileName.set("page.js")
            }
        }

        compilerOptions {
            target.set("es2015")
        }

        binaries.executable()
    }

    fun dependencies(fn: ComposeDependencyHandler.() -> Unit) {
        base.mpp.sourceSets.named("webMain") {
            dependencies { object : ComposeDependencyHandler, KotlinDependencyHandler by this {}.fn() }
        }
    }

    fun testDependencies(fn: ComposeDependencyHandler.() -> Unit) {
        base.mpp.sourceSets.named("webTest") {
            dependencies { object : ComposeDependencyHandler, KotlinDependencyHandler by this {}.fn() }
        }
    }
}
