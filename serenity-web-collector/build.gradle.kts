plugins {
    kotlin("jvm")
    id("published-jvm")
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(project(":"))
    implementation(project(":serenity-collector-lib"))
    implementation(libs.ksp.symbolProcessingApi)
}
