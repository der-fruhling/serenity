plugins {
    kotlin("jvm")
    id("published-jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":serenity-annotations"))
    implementation(libs.ksp.symbolProcessingApi)
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}