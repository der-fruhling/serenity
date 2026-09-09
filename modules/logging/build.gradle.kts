plugins {
    id("multiplatform-base")
    id("net.derfruhling.serenity.convention")
    id("net.derfruhling.serenity.compiler-plugin")
    id("published")
    alias(libs.plugins.kotlinx.atomicfu)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":serenity-annotations"))
                api(libs.kotlinx.coroutines.core)
                api(libs.oshai.kotlinLogging)
                api(libs.kotlinx.datetime)
            }
        }

        commonTest {
            dependencies {
                implementation(project(":serenity-test"))
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
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

        nativeMain {
            dependencies {
                api(libs.kotlinx.atomicfu)
                api(libs.kotlinx.io)
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
