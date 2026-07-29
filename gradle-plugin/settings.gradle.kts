plugins {
    id("com.gradle.develocity") version "4.5.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "serenity-gradle-plugin"

include("serenity-sass-gradle-plugin")
project(":serenity-sass-gradle-plugin").projectDir = file("sass-plugin")
