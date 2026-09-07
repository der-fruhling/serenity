plugins {
    id("net.derfruhling.serenity.base") apply false
    id("net.derfruhling.serenity.web") apply false
    id("net.derfruhling.serenity.server") apply false
    id("net.derfruhling.serenity.stylist-sass") apply false
    id("net.derfruhling.serenity.convention") apply false
    id("net.derfruhling.serenity") apply false
    id("net.derfruhling.serenity.resources") apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.node.gradle) apply false

    `version-catalog`
    id("published")
}

allprojects {
    group = "net.derfruhling.serenity"

    apply(from = rootProject.file("common.gradle.kts"))
}

catalog {
    versionCatalog {
        from(files("gradle/serenity.versions.toml"))

        version("serenity", project.version.toString())
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["versionCatalog"])
        }
    }
}
