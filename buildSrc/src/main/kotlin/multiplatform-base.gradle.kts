import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
}

plugins.withId("com.google.devtools.ksp") {
    afterEvaluate {
        tasks.named("sourcesJar").configure { dependsOn("kspCommonMainKotlinMetadata", "kspServerMainKotlinMetadata", "kspWebMainKotlinMetadata") }
        tasks.named("linuxX64SourcesJar").configure { dependsOn("kspKotlinLinuxX64", "kspCommonMainKotlinMetadata", "kspServerMainKotlinMetadata") }
        tasks.named("linuxArm64SourcesJar").configure { dependsOn("kspKotlinLinuxArm64", "kspCommonMainKotlinMetadata", "kspServerMainKotlinMetadata") }
        tasks.named("macosArm64SourcesJar").configure { dependsOn("kspKotlinMacosArm64", "kspCommonMainKotlinMetadata", "kspServerMainKotlinMetadata") }
        tasks.named("jvmSourcesJar").configure { dependsOn("kspKotlinJvm", "kspCommonMainKotlinMetadata", "kspServerMainKotlinMetadata") }
        tasks.named("jsSourcesJar").configure { dependsOn("kspKotlinJs", "kspCommonMainKotlinMetadata", "kspWebMainKotlinMetadata") }
        tasks.named("wasmJsSourcesJar").configure { dependsOn("kspKotlinWasmJs", "kspCommonMainKotlinMetadata", "kspWebMainKotlinMetadata") }
        tasks.named("compileKotlinLinuxX64").configure { dependsOn("kspKotlinLinuxX64", "kspCommonMainKotlinMetadata", "kspServerMainKotlinMetadata") }
        tasks.named("compileKotlinLinuxArm64").configure { dependsOn("kspKotlinLinuxArm64", "kspCommonMainKotlinMetadata", "kspServerMainKotlinMetadata") }
        tasks.named("compileKotlinMacosArm64").configure { dependsOn("kspKotlinMacosArm64", "kspCommonMainKotlinMetadata", "kspServerMainKotlinMetadata") }
        tasks.named("compileKotlinJvm").configure { dependsOn("kspKotlinJvm", "kspCommonMainKotlinMetadata", "kspServerMainKotlinMetadata") }
        tasks.named("compileKotlinJs").configure { dependsOn("kspKotlinJs", "kspCommonMainKotlinMetadata", "kspWebMainKotlinMetadata") }
        tasks.named("compileKotlinWasmJs").configure { dependsOn("kspKotlinWasmJs", "kspCommonMainKotlinMetadata", "kspWebMainKotlinMetadata") }
    }
}

kotlin {
    jvmToolchain(17)
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
                    //useFirefoxHeadless()
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
                    //useFirefoxHeadless()
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
