
// The settings file is the entry point of every Gradle build.
// Its primary purpose is to define the subprojects.
// It is also used for some aspects of project-wide configuration, like managing plugins, dependencies, etc.
// https://docs.gradle.org/current/userguide/settings_file_basics.html

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()

        maven("https://maven.pkg.github.com/der-fruhling/serene-wasm") {
            name = "GitHubPackages"

            credentials {
                val props = java.util.Properties()
                gradle.gradleUserHomeDir.resolve("gradle.properties").inputStream().use {
                    props.load(it)
                }

                file("gradle.properties").inputStream().use {
                    props.load(it)
                }

                username = props.getProperty("gpr.user") ?: System.getenv("GH_USERNAME")
                password = props.getProperty("gpr.key") ?: System.getenv("GK_TOKEN")
            }
        }
    }

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

fun library(name: String, dirName: String = name) {
    include(name)
    project(":$name").projectDir = file("modules").resolve(dirName)
}

fun collector(name: String, dirName: String = name) {
    include("serenity-$name")
    project(":serenity-$name").projectDir = file("modules").resolve("collectors").resolve(dirName)
}

module("compiler-plugin")
module("annotations")
module("core")
module("logging")
module("dynamic")
module("localization")
module("inline-style")
module("test")
module("ktor-server")

collector("collector-lib", "lib")
collector("platform-collector", "platform")
collector("common-collector", "common")
collector("ktor-collector", "ktor")
collector("web-collector", "web")

include("test-app")

dependencyResolutionManagement {
    versionCatalogs {
        create("serenityLibs") {
            from(files("gradle/serenity.versions.toml"))
        }
    }
}
