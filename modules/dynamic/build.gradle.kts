import org.jetbrains.kotlin.gradle.tasks.BaseKotlinCompile

plugins {
    id("multiplatform-compose")
    id("net.derfruhling.serenity.convention")
    id("net.derfruhling.serenity.compiler-plugin")
    id("published")
    id("com.google.devtools.ksp")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":serenity-core"))
                api(libs.kotlinx.serialization.cbor)
            }
        }

        commonTest {
            dependencies {
                implementation(project(":serenity-test"))
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
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

dependencies {
    add("kspJvm", project(":serenity-platform-collector"))
    add("kspLinuxX64", project(":serenity-platform-collector"))
    add("kspLinuxArm64", project(":serenity-platform-collector"))
    add("kspMacosArm64", project(":serenity-platform-collector"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
