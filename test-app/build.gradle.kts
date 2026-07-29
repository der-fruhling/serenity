import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("net.derfruhling.serenity")
    id("net.derfruhling.serenity.stylist-sass")
    id("net.derfruhling.serenity.convention")
    id("net.derfruhling.serenity.resources")
}

dependencies {
    resources(project(":serenity-core"))
}

serenity {
    javaVersion = 25
    disableDefaultDependencies = true

    dependencies {
        implementation(project(":serenity-core"))
        implementation(libs.androidx.compose.runtime)
        implementation(libs.androidx.compose.runtime.saveable)
        implementation(libs.oshai.kotlinLogging)
    }

    testDependencies {
        implementation(kotlin("test"))
    }

    resources {
        prettyJson = true
    }

    collectors {
        useCommon(project(":serenity-common-collector"))
        useWeb(project(":serenity-web-collector"))
        useServer(project(":serenity-ktor-collector"))
    }

    server {
        nativeEntryPoint = "net.derfruhling.serenity.testapp.main"

        jvm()
        macosArm64()
        linuxArm64()
        linuxX64()

        dependencies {
            common {
                implementation(project(":serenity-ktor-server"))
            }

            jvm {
                implementation(libs.ktor.server.netty)
                implementation(libs.logback.classic)
            }

            native {
                implementation(libs.ktor.server.cio)
            }
        }

        testDependencies {
            jvm {
                compileOnly(libs.junit.api)
                runtimeOnly(libs.junit.engine)
            }
        }
    }

    web {
        js()
        @OptIn(ExperimentalWasmDsl::class)
        wasmJs()

        dependencies {
            implementation(libs.kotlin.wrappers.js)
            implementation(libs.kotlin.wrappers.browser)
            implementation(libs.kotlin.wrappers.web)
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
    enableAssertions = true
}
