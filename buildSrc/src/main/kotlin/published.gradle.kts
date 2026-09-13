plugins {
    `maven-publish`
}

publishing {
    repositories {
        maven(rootProject.layout.buildDirectory.dir("local-publish")) {
            name = "LocalDirectory"
        }

        maven("https://maven.pkg.github.com/der-fruhling/serenity") {
            name = "GithubPackages"

            credentials {
                username = project.findProperty("gpr.user")?.toString() ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key")?.toString() ?: System.getenv("TOKEN")
            }
        }
    }
}
