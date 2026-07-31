package net.derfruhling.serenity.gradle.web

import net.derfruhling.serenity.gradle.SerenityDependencyHandler
import net.derfruhling.serenity.gradle.SerenityExtension
import net.derfruhling.serenity.gradle.SerenityGradleDsl
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

@SerenityGradleDsl
abstract class SerenityWebExtension(internal val base: SerenityExtension) : ExtensionAware {
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
        base.webTargets.add(this.name)

        browser {
            webpackTask {
                if (mode == KotlinWebpackConfig.Mode.PRODUCTION) {
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
