import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvmToolchain(25)
    jvm()

    macosArm64()
    linuxArm64()
    linuxX64()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyHierarchyTemplate {
        common {
            withJvm()

            group("native") {
                group("linux") {
                    withLinuxX64()
                    withLinuxArm64()
                }

                group("macos") {
                    withMacosArm64()
                }
            }
        }
    }
}
