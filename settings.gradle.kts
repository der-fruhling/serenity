// The settings file is the entry point of every Gradle build.
// Its primary purpose is to define the subprojects.
// It is also used for some aspects of project-wide configuration, like managing plugins, dependencies, etc.
// https://docs.gradle.org/current/userguide/settings_file_basics.html

pluginManagement {
    includeBuild("gradle-plugin")
}

plugins {
    id("com.gradle.develocity") version "4.5.0"
    // Use the Foojay Toolchains plugin to automatically download JDKs required by subprojects.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "serenity"

fun module(name: String, dirName: String = name) {
    include("serenity-$name")
    project(":serenity-$name").projectDir = file("modules").resolve(dirName)
}

fun collector(name: String, dirName: String = name) {
    include("serenity-$name")
    project(":serenity-$name").projectDir = file("modules").resolve("collectors").resolve(dirName)
}

module("annotations")
module("core")
module("ktor-server")

collector("collector-lib", "lib")
collector("common-collector", "common")
collector("ktor-collector", "ktor")
collector("web-collector", "web")

include("test-app")
