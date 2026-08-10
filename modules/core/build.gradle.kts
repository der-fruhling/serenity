plugins {
    id("multiplatform-compose")
    id("net.derfruhling.serenity.convention")
    id("published")
    id("net.derfruhling.serenity.resources")
    id("net.derfruhling.serenity.stylist-sass")
}

allprojects {
    group = "net.derfruhling.serenity"

    apply(from = rootProject.file("common.gradle.kts"))
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":serenity-annotations"))
                api(libs.kotlinx.coroutines.core)
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

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
