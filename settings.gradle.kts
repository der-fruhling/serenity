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

rootProject.name = "compose-html"

include("compose-html-ktor-server")
include("compose-html-common-collector")
include("compose-html-ktor-collector")
include("compose-html-web-collector")
include("test-app")

develocity {
    server
}

include("compose-html-collector-lib")