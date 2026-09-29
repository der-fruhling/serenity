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

        maven("https://maven.amphoreus.info/repository/maven-release") {
            name = "Nexus"
        }

        maven("https://maven.amphoreus.info/repository/maven-snapshot") {
            name = "NexusSnapshot"
        }
    }

    plugins.withType<PublishingPlugin> {
        configure<PublishingExtension> {
            repositories {
                maven(rootProject.layout.projectDirectory.dir("../build/local-publish")) {
                    name = "LocalDirectory"
                }
            }
        }

        afterEvaluate {
            val repoName = if("SNAPSHOT" in (version as String)) {
                "maven-snapshot"
            } else {
                "maven-release"
            }

            configure<PublishingExtension> {
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
