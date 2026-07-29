import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("net.derfruhling.serenity.base")
    id("published")
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
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":"))
                api(libs.ktor.server.core)
                api(libs.ktor.server.conditionalHeaders)
                api(libs.kotlinx.collections.immutable)
                api(libs.oshai.kotlinLogging)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        jvmTest {
            dependencies {
                compileOnly(libs.junit.api)
                runtimeOnly(libs.junit.engine)
            }
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
    enableAssertions = true
}
