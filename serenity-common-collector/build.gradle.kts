plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(rootProject)
    implementation(project(":serenity-collector-lib"))
    implementation(libs.ksp.symbolProcessingApi)
}
