plugins {
    id("multiplatform-compose")
    id("net.derfruhling.serenity.convention")
    id("net.derfruhling.serenity.compiler-plugin")
    id("published")
    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlinx.atomicfu)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":serenity-annotations"))
                api(project(":serenity-core"))
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.io)
                api(libs.kotlinx.atomicfu)
                api(libs.androidx.compose.runtime)
                api(libs.androidx.compose.runtime.saveable)
                api(libs.androidx.collections)
                api(libs.oshai.kotlinLogging)

                api(libs.kotlinx.serialization.core)
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
