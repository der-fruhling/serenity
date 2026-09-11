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
                api(project(":serenity-annotations"))
                api(project(":serenity-core"))
                api(project(":serenity-channel"))
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.io)
                api(libs.androidx.compose.runtime)
                api(libs.androidx.compose.runtime.saveable)
                api(libs.androidx.collections)
                api(libs.oshai.kotlinLogging)

                api(libs.ktor.common.websockets)

                api(libs.kotlinx.serialization.core)
                api(libs.kotlinx.serialization.json)
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

        serverMain {
            dependencies {
                api(libs.ktor.server.core)
                api(libs.ktor.server.websockets)
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
                api(libs.ktor.client.core)
                api(libs.ktor.client.js)
                api(libs.ktor.client.websockets)

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
