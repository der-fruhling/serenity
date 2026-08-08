plugins {
    id("multiplatform-compose")
    id("net.derfruhling.serenity.convention")
    id("com.google.devtools.ksp")
    id("io.kotest")
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
                api(libs.kotest.engine)
                api(libs.kotest.assertions)
                api(libs.kotest.property)
            }
        }

        jvmMain {
            dependencies {
                api(libs.kotest.runner.junit5)
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
