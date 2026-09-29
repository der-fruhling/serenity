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

afterEvaluate {
    val repoName = if("SNAPSHOT" in (project.version as String)) {
        "maven-snapshot"
    } else {
        "maven-release"
    }

    publishing {
        repositories {
            maven("https://maven.amphoreus.info/repository/$repoName") {
                name = "Nexus"

                credentials {
                    username = project.findProperty("aanexus.user")?.toString() ?: System.getenv("AA_USERNAME")
                    password = project.findProperty("aanexus.key")?.toString() ?: System.getenv("AA_PASSWORD")
                }
            }
        }
    }
}
