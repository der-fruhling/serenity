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
// Cache build artifacts, so expensive operations do not need to be re-computed
buildCache {
    local {
        isEnabled = !isCiServer
    }
}

develocity {
    buildScan {
        if (isCiServer) {
            tag("CI")
        }
    }
}
