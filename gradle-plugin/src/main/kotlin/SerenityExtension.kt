package net.derfruhling.serenity.gradle

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

@SerenityGradleDsl
abstract class SerenityExtension(internal val mpp: KotlinMultiplatformExtension) : ExtensionAware {
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

    fun dependencies(fn: SerenityDependencyHandler.() -> Unit) {
        mpp.sourceSets.named("commonMain") {
            dependencies { object : SerenityDependencyHandler, KotlinDependencyHandler by this {}.fn() }
        }
    }

    fun testDependencies(fn: SerenityDependencyHandler.() -> Unit) {
        mpp.sourceSets.named("commonTest") {
            dependencies { object : SerenityDependencyHandler, KotlinDependencyHandler by this {}.fn() }
        }
    }
}
