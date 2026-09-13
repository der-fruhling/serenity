plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("plugin.serialization") version embeddedKotlinVersion
    `maven-publish`
    alias(libs.plugins.buildconfig)
}

buildConfig {
    packageName("net.derfruhling.serenity.gradle")

    buildConfigField("VERSION", provider { project.version.toString() })
    buildConfigField("KOTLIN_VERSION", libs.versions.kotlin.asProvider())
}

allprojects {
    group = "net.derfruhling.serenity.gradle"

    apply(from = rootProject.file("../common.gradle.kts"))

    tasks.withType(Test::class.java).configureEach {
        failOnNoDiscoveredTests = false
    }

    repositories {
        gradlePluginPortal()

        maven("https://maven.pkg.github.com/der-fruhling/serene-wasm") {
            name = "GitHubPackages"

            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }

    plugins.withType<PublishingPlugin> {
        configure<PublishingExtension> {
            repositories {
                maven(rootProject.layout.projectDirectory.dir("../build/local-publish")) {
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
    }
}

gradlePlugin {
    plugins {
        fun new(name: String?, implClass: String) {
            create(name ?: "main") {
                id = "net.derfruhling.serenity${name?.let { ".$it" } ?: ""}"
                implementationClass = "net.derfruhling.serenity.gradle.$implClass"
            }
        }

        new("base", "SerenityBasePlugin")
        new("compiler-plugin", "SerenityCompilerPlugin")
        new("server", "server.SerenityServerPlugin")
        new("web", "web.SerenityWebPlugin")
        new("convention", "SerenityConventionPlugin")
        new("resources", "resources.SerenityResourcesPlugin")
        new("localization", "resources.SerenityLocalizationPlugin")
        new(null, "SerenityPlugin")
    }
}

dependencies {
    api(plugin(libs.plugins.kotlin.multiplatform))
    api(plugin(libs.plugins.kotlin.plugin.compose))
    api(plugin(libs.plugins.kotlin.plugin.serialization))
    api(plugin(libs.plugins.kotlin.ksp))
    api(libs.serene.wasm)
    implementation(libs.openhft.zeroAllocationHashing)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.cbor)
    implementation(libs.ktoml.core)
}

sourceSets.main {
    resources.srcDir(project.layout.buildDirectory.dir("generated-resources"))
}

fun plugin(p: Provider<PluginDependency>): Provider<String> = p.map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}
