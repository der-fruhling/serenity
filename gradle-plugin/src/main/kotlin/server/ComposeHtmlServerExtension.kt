package net.derfruhling.html.gradle.server

import net.derfruhling.html.gradle.ComposeDependencyHandler
import net.derfruhling.html.gradle.ComposeDependencyHandlerImpl
import net.derfruhling.html.gradle.ComposeHtmlDsl
import net.derfruhling.html.gradle.ComposeHtmlExtension
import org.gradle.api.Action
import org.gradle.api.InvalidUserCodeException
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

@ComposeHtmlDsl
abstract class ComposeHtmlServerExtension(internal val base: ComposeHtmlExtension) : ExtensionAware {
    abstract val createDefaultBinaries: Property<Boolean>
    abstract val nativeEntryPoint: Property<String>

    init {
        createDefaultBinaries.convention(true)
    }

    fun jvm() {
        base.mpp.jvmToolchain(base.javaVersion.get())
        base.mpp.jvm()
    }

    fun jvm(configure: Action<KotlinJvmTarget>) {
        base.mpp.jvmToolchain(base.javaVersion.get())
        base.mpp.jvm(configure)
    }

    private fun KotlinNativeTarget.configureNativeTarget() {
        if(createDefaultBinaries.get()) {
            binaries {
                executable {
                    entryPoint(this@ComposeHtmlServerExtension.nativeEntryPoint.orNull
                        ?: throw InvalidUserCodeException("If createDefaultBinaries is not explicitly disabled, you must set entryPoint"))
                }
            }
        }
    }

    fun linuxX64() {
        base.mpp.linuxX64 { configureNativeTarget() }
    }

    fun linuxArm64() {
        base.mpp.linuxArm64 { configureNativeTarget() }
    }

    fun macosArm64() {
        base.mpp.macosArm64 { configureNativeTarget() }
    }

    @ComposeHtmlDsl
    interface ServerDependencyHandler {
        fun common(fn: ComposeDependencyHandler.() -> Unit)
        fun jvm(fn: ComposeDependencyHandler.() -> Unit)
        fun native(fn: ComposeDependencyHandler.() -> Unit)
        fun linuxX64(fn: ComposeDependencyHandler.() -> Unit)
        fun linuxArm64(fn: ComposeDependencyHandler.() -> Unit)
        fun macosArm64(fn: ComposeDependencyHandler.() -> Unit)
    }

    fun dependencies(fn: ServerDependencyHandler.() -> Unit) {
        ServerDependencyHandlerImpl("Main").fn()
    }

    fun testDependencies(fn: ServerDependencyHandler.() -> Unit) {
        ServerDependencyHandlerImpl("Test").fn()
    }

    private inner class ServerDependencyHandlerImpl(val suffix: String) : ServerDependencyHandler {
        override fun common(fn: ComposeDependencyHandler.() -> Unit) {
            this@ComposeHtmlServerExtension.base.mpp.sourceSets.named("server$suffix") {
                dependencies { ComposeDependencyHandlerImpl(this).fn() }
            }
        }

        override fun jvm(fn: ComposeDependencyHandler.() -> Unit) {
            this@ComposeHtmlServerExtension.base.mpp.sourceSets.named("jvm$suffix") {
                dependencies { ComposeDependencyHandlerImpl(this).fn() }
            }
        }

        override fun native(fn: ComposeDependencyHandler.() -> Unit) {
            this@ComposeHtmlServerExtension.base.mpp.sourceSets.named("native$suffix") {
                dependencies { ComposeDependencyHandlerImpl(this).fn() }
            }
        }

        override fun linuxX64(fn: ComposeDependencyHandler.() -> Unit) {
            this@ComposeHtmlServerExtension.base.mpp.sourceSets.named("linuxX64$suffix") {
                dependencies { ComposeDependencyHandlerImpl(this).fn() }
            }
        }

        override fun linuxArm64(fn: ComposeDependencyHandler.() -> Unit) {
            this@ComposeHtmlServerExtension.base.mpp.sourceSets.named("linuxArm64$suffix") {
                dependencies { ComposeDependencyHandlerImpl(this).fn() }
            }
        }

        override fun macosArm64(fn: ComposeDependencyHandler.() -> Unit) {
            this@ComposeHtmlServerExtension.base.mpp.sourceSets.named("macosArm64$suffix") {
                dependencies { ComposeDependencyHandlerImpl(this).fn() }
            }
        }
    }
}
