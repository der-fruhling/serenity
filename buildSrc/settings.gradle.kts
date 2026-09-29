plugins {
    id("com.gradle.develocity")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

val isCiServer = System.getenv().containsKey("CI")
val isPublishBuildCacheServer = System.getenv().containsKey("PUBLISH_BUILD_CACHE")
// Cache build artifacts, so expensive operations do not need to be re-computed
buildCache {
    local {
        isEnabled = !isCiServer
    }

    remote<HttpBuildCache> {
        url = uri("https://maven.amphoreus.info/repository/gradle-build-cache/")

        if(isCiServer || isPublishBuildCacheServer) {
            isPush = true
            credentials {
                val props = java.util.Properties()
                val globalProps = gradle.gradleUserHomeDir.resolve("gradle.properties")

                if(globalProps.exists()) {
                    globalProps.inputStream().use {
                        props.load(it)
                    }
                }

                file("gradle.properties").inputStream().use {
                    props.load(it)
                }

                username = props.getProperty("aanexus.user") ?: System.getenv("AA_USERNAME")
                password = props.getProperty("aanexus.key") ?: System.getenv("AA_PASSWORD")
            }
        } else {
            isPush = false
        }
    }
}

develocity {
    buildScan {
        if (isCiServer) {
            tag("CI")
        }
    }
}
