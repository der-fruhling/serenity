import io.freefair.gradle.plugins.sass.SassCompile
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("net.derfruhling.serenity.resources")
    id("net.derfruhling.serenity.stylist-sass")
    alias(libs.plugins.stabilityAnalyzer)
    `maven-publish`
}

allprojects {
    group = "net.derfruhling.serenity"

    apply(from = rootProject.file("common.gradle.kts"))
}

publishing {
    repositories {
        maven(rootProject.layout.buildDirectory.dir("local-publish")) {
            name = "LocalDirectory"
        }
    }
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
                    useFirefoxHeadless()
                    useChromiumHeadless()
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
                    useFirefoxHeadless()
                    useChromiumHeadless()
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

    sourceSets {
        commonMain {
            dependencies {
                api(libs.androidx.compose.runtime)
                api(libs.androidx.compose.runtime.saveable)
                api(libs.androidx.collections)
                api(libs.oshai.kotlinLogging)
                api(libs.kotlinx.datetime)

                api(libs.kotlinx.serialization.core)
                api(libs.kotlinx.serialization.json)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        named("serverMain") {
            dependencies {
                api(libs.ksoup)
                api(libs.kotlinx.io)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.slf4j.api)
            }
        }

        jvmTest {
            dependencies {
                compileOnly(libs.junit.api)
                runtimeOnly(libs.junit.engine)
            }
        }

        webMain {
            dependencies {
                api(libs.kotlin.wrappers.js)
                api(libs.kotlin.wrappers.browser)
                api(libs.kotlin.wrappers.web)
            }
        }
    }
}
