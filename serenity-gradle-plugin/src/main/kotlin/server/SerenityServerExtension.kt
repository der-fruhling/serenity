package net.derfruhling.serenity.gradle.server

import net.derfruhling.serenity.gradle.SerenityDependencyHandler
import net.derfruhling.serenity.gradle.SerenityDependencyHandlerImpl
import net.derfruhling.serenity.gradle.SerenityExtension
import net.derfruhling.serenity.gradle.SerenityGradleDsl
import org.gradle.api.Action
import org.gradle.api.InvalidUserCodeException
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import javax.inject.Inject

@SerenityGradleDsl
abstract class SerenityServerExtension(internal val base: SerenityExtension) : ExtensionAware {
    abstract val createDefaultBinaries: Property<Boolean>
    abstract val createDistTasks: Property<Boolean>
    abstract val nativeEntryPoint: Property<String>
    abstract val resourceTask: Property<String>
    abstract val debugResourceTask: Property<String>

    @get:Optional
    abstract val composeApplicationManifestTask: Property<String>

    @get:Optional
    abstract val composeApplicationManifestDebugTask: Property<String>

    @get:Inject
    abstract val project: Project

    private val logger = Logging.getLogger(SerenityServerExtension::class.java)!!

    init {
        createDefaultBinaries.convention(true)
        createDistTasks.convention(true)

        val distDir = project.layout.buildDirectory.dir("resources/all")
        fun syncServerResourcesTask(suffix: String, dirSuffix: String): TaskProvider<Sync> = project.tasks.register("syncServerResources$suffix", Sync::class) {
            into(distDir)

            from(
                project.layout.buildDirectory.dir("resources/common$dirSuffix"),
                project.layout.buildDirectory.dir("resources/server$dirSuffix")
            )
            dependsOn(
                "processCommonResources$suffix",
                "processServerResources$suffix"
            )

            inputs.dir(project.layout.buildDirectory.dir("resources/common$dirSuffix"))
            inputs.dir(project.layout.buildDirectory.dir("resources/server$dirSuffix"))
            outputs.dir(distDir)
        }

        debugResourceTask.convention(syncServerResourcesTask("Debug", "-debug").name)
        resourceTask.convention(syncServerResourcesTask("", "").name)
    }

    private fun KotlinTarget.configureTarget() {
        base.serverTargets.add(this.name)
    }

    fun jvm() {
        base.mpp.jvmToolchain(base.javaVersion.get())
        base.mpp.jvm { configureTarget() }
    }

    fun jvm(configure: Action<KotlinJvmTarget>) {
        base.mpp.jvmToolchain(base.javaVersion.get())
        base.mpp.jvm {
            configureTarget()
            configure.execute(this)
        }
    }

    private fun KotlinNativeTarget.configureNativeTarget() {
        configureTarget()

        if(createDefaultBinaries.get()) {
            binaries {
                executable {
                    entryPoint(this@SerenityServerExtension.nativeEntryPoint.orNull
                        ?: throw InvalidUserCodeException("If createDefaultBinaries is not explicitly disabled, you must set entryPoint"))
                }
            }
        }

        if(createDistTasks.get()) {
            val releaseBinary = binaries.findExecutable(NativeBuildType.RELEASE)

            if(releaseBinary != null) {
                val distDir = project.layout.buildDirectory.dir("dist/${name}")
                val targetName = name

                val prepareDist = project.tasks.register("${name}PrepareDist", Sync::class) {
                    description = "Prepares the $targetName release-mode server for execution"
                    group = "dist"

                    into(distDir)
                    from(releaseBinary.linkTaskProvider)

                    if(composeApplicationManifestTask.isPresent) {
                        val task = project.tasks.named(composeApplicationManifestTask.get())
                        dependsOn(task)
                        from(task)
                    }

                    dependsOn(resourceTask)

                    val resourceTask = project.tasks.named(resourceTask.get())
                    inputs.files(resourceTask)
                    outputs.dir(distDir)

                    into("_static") {
                        from(resourceTask) {
                            exclude("resource-index.json")
                        }
                    }
                }

                val debugBinary = binaries.findExecutable(NativeBuildType.DEBUG)

                if(debugBinary != null) {
                    val runDir = project.layout.buildDirectory.dir("run/${name}")
                    val prepareDebug = project.tasks.register("${name}PrepareDebug", Sync::class) {
                        description = "Prepares the $targetName debug-mode server for execution"
                        group = "run"

                        into(runDir)
                        from(debugBinary.linkTaskProvider)

                        if(composeApplicationManifestDebugTask.isPresent) {
                            val task = project.tasks.named(composeApplicationManifestDebugTask.get())
                            dependsOn(task)
                            from(task)
                        }

                        val debugResourceTask = project.tasks.named(debugResourceTask.get())
                        dependsOn(debugResourceTask)

                        inputs.files(debugResourceTask)
                        outputs.dir(runDir)

                        into("_static") {
                            from(debugResourceTask)
                        }
                    }

                    project.tasks.register("${name}Run", Exec::class) {
                        description = "Runs the $targetName debug executable"
                        group = "run"

                        dependsOn(prepareDebug)

                        // TODO: windows should use .exe
                        executable(runDir.map { it.file(debugBinary.baseName + ".kexe").asFile.absolutePath })
                        workingDir(runDir)
                    }
                }

                project.tasks.register("${name}DistZip", Zip::class) {
                    description = "Archives the $targetName distribution into a .zip file"
                    group = "dist"

                    destinationDirectory.set(project.layout.buildDirectory.dir("dist"))
                    archiveClassifier.set(targetName)

                    from(prepareDist)
                }

                project.tasks.register("${name}DistTar", Tar::class) {
                    description = "Archives the $targetName distribution into a .tgz file"
                    group = "dist"
                    compression = Compression.GZIP

                    destinationDirectory.set(project.layout.buildDirectory.dir("dist"))
                    archiveClassifier.set(targetName)

                    from(prepareDist)
                }
            } else {
                logger.warn("Native target $name has no executable defined, not generating dist tasks. If you meant to do this, try setting createDistTasks to false in composeHtml.server")
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

    @SerenityGradleDsl
    interface ServerDependencyHandler {
        fun common(fn: SerenityDependencyHandler.() -> Unit)
        fun jvm(fn: SerenityDependencyHandler.() -> Unit)
        fun native(fn: SerenityDependencyHandler.() -> Unit)
        fun linuxX64(fn: SerenityDependencyHandler.() -> Unit)
        fun linuxArm64(fn: SerenityDependencyHandler.() -> Unit)
        fun macosArm64(fn: SerenityDependencyHandler.() -> Unit)
    }

    fun dependencies(fn: ServerDependencyHandler.() -> Unit) {
        ServerDependencyHandlerImpl("Main").fn()
    }

    fun testDependencies(fn: ServerDependencyHandler.() -> Unit) {
        ServerDependencyHandlerImpl("Test").fn()
    }

    private inner class ServerDependencyHandlerImpl(val suffix: String) : ServerDependencyHandler {
        override fun common(fn: SerenityDependencyHandler.() -> Unit) {
            this@SerenityServerExtension.base.mpp.sourceSets.named("server$suffix") {
                dependencies { SerenityDependencyHandlerImpl(this).fn() }
            }
        }

        override fun jvm(fn: SerenityDependencyHandler.() -> Unit) {
            this@SerenityServerExtension.base.mpp.sourceSets.named("jvm$suffix") {
                dependencies { SerenityDependencyHandlerImpl(this).fn() }
            }
        }

        override fun native(fn: SerenityDependencyHandler.() -> Unit) {
            this@SerenityServerExtension.base.mpp.sourceSets.named("native$suffix") {
                dependencies { SerenityDependencyHandlerImpl(this).fn() }
            }
        }

        override fun linuxX64(fn: SerenityDependencyHandler.() -> Unit) {
            this@SerenityServerExtension.base.mpp.sourceSets.named("linuxX64$suffix") {
                dependencies { SerenityDependencyHandlerImpl(this).fn() }
            }
        }

        override fun linuxArm64(fn: SerenityDependencyHandler.() -> Unit) {
            this@SerenityServerExtension.base.mpp.sourceSets.named("linuxArm64$suffix") {
                dependencies { SerenityDependencyHandlerImpl(this).fn() }
            }
        }

        override fun macosArm64(fn: SerenityDependencyHandler.() -> Unit) {
            this@SerenityServerExtension.base.mpp.sourceSets.named("macosArm64$suffix") {
                dependencies { SerenityDependencyHandlerImpl(this).fn() }
            }
        }
    }
}
