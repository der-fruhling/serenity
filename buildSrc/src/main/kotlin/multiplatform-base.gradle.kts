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

    js {
        browser {
            commonWebpackConfig {
                sourceMaps = true
            }
            testTask {
                useKarma {
                    useChromiumHeadless()
                    useFirefoxHeadless()
                }
            }
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                sourceMaps = true
            }

            testTask {
                useKarma {
                    useChromiumHeadless()
                    useFirefoxHeadless()
                }
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyHierarchyTemplate {
        common {
            group("server") {
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

            group("web") {
                withWasmJs()
                withJs()
            }
        }
    }
}
