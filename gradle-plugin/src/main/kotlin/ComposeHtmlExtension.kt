package net.derfruhling.html.gradle

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.dsl.KotlinDependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

@ComposeHtmlDsl
abstract class ComposeHtmlExtension(internal val mpp: KotlinMultiplatformExtension) : ExtensionAware {
    abstract val javaVersion: Property<Int>
    abstract val composeHtmlVersion: Property<String>
    abstract val disableDefaultDependencies: Property<Boolean>

    init {
        javaVersion.convention(25)
        composeHtmlVersion.convention("0.1.0-SNAPSHOT")
        disableDefaultDependencies.convention(false)
    }

    fun disableDefaultDependencies() {
        disableDefaultDependencies.set(true)
    }

    fun dependencies(fn: ComposeDependencyHandler.() -> Unit) {
        mpp.sourceSets.named("commonMain") {
            dependencies { object : ComposeDependencyHandler, KotlinDependencyHandler by this {}.fn() }
        }
    }

    fun testDependencies(fn: ComposeDependencyHandler.() -> Unit) {
        mpp.sourceSets.named("commonTest") {
            dependencies { object : ComposeDependencyHandler, KotlinDependencyHandler by this {}.fn() }
        }
    }
}
