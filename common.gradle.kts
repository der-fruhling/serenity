version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()

    exclusiveContent {
        forRepository {
            maven(rootProject.layout.buildDirectory.dir("local-publish")) {
                name = "LocalDirectory"
            }
        }

        filter {
            includeModule("net.derfruhling.serenity", "serenity-annotations")
            includeModule("net.derfruhling.serenity", "serenity-annotations-jvm")
            includeModule("net.derfruhling.serenity", "serenity-compiler-plugin")
        }
    }
}


