plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(rootProject)
    implementation(project(":compose-html-collector-lib"))
    implementation(libs.ksp.symbolProcessingApi)
}
