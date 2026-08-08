import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform") version "2.3.0"
}

repositories {
    mavenCentral()
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

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.io)
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

