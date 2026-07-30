plugins {
    kotlin("jvm")
    id("published-jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":serenity-annotations"))
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}