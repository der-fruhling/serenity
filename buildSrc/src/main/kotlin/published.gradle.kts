plugins {
    `maven-publish`
}

publishing {
    repositories {
        maven(rootProject.layout.buildDirectory.dir("local-publish")) {
            name = "LocalDirectory"
        }
    }
}
