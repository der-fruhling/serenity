plugins {
    id("multiplatform-compose")
    id("net.derfruhling.serenity.convention")
    id("published")
}

allprojects {
    group = "net.derfruhling.serenity"

    apply(from = rootProject.file("common.gradle.kts"))
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":serenity-core"))
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.coroutines.test)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
