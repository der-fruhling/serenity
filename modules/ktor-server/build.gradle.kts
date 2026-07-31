plugins {
    id("multiplatform-server-compose")
    id("net.derfruhling.serenity.convention")
    id("published")
}

repositories {
    mavenCentral()
    google()
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":serenity-core"))
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
