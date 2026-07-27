import io.freefair.gradle.plugins.sass.SassCompile
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.sassBase)
//    alias(libs.plugins.stabilityAnalyzer)
}

repositories {
    mavenCentral()
    google()
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
                api(libs.kermit)
                api(libs.kotlinx.datetime)

                api(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)
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
                implementation(libs.kotlin.wrappers.js)
                implementation(libs.kotlin.wrappers.browser)
                implementation(libs.kotlin.wrappers.web)
            }
        }
    }
}

val resourcesDir = fileTree("src/commonMain/resources/style")
val sassOutDir = layout.buildDirectory.dir("sass")

val compileSass = tasks.register<SassCompile>("compileSass") {
    description = "Compiles stylesheets"

    source(resourcesDir)
    destinationDir.set(sassOutDir)
    group = BasePlugin.BUILD_GROUP
}

val resourcesOutPath = layout.buildDirectory.dir("resources")
val resourcesIn = kotlin.sourceSets.commonMain.map { it.resources }

val processResources = tasks.register<ProcessResources>("processResources") {
    dependsOn(compileSass)

    into(resourcesOutPath)

    from(resourcesIn) {
        exclude("style/**")
    }

    into("style") {
        from(sassOutDir)
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
    enableAssertions = true
}
