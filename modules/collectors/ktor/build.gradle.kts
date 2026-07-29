plugins {
    kotlin("jvm")
    id("published-jvm")
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(project(":serenity-annotations"))
    implementation(project(":serenity-collector-lib"))
    implementation(libs.ksp.symbolProcessingApi)
}
