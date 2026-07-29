plugins {
    id("multiplatform-base")
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
                implementation(libs.androidx.compose.runtime)
                api(libs.kotlinx.datetime)
            }
        }
    }
}
