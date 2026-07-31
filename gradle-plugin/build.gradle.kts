plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("plugin.serialization") version embeddedKotlinVersion
    `maven-publish`
}

allprojects {
    group = "net.derfruhling.serenity.gradle"

    apply(from = rootProject.file("../common.gradle.kts"))

    repositories {
        gradlePluginPortal()
    }
}

publishing {
    repositories {
        maven(rootProject.layout.projectDirectory.dir("../build/local-publish")) {
            name = "LocalDirectory"
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
        new("server", "server.SerenityServerPlugin")
        new("web", "web.SerenityWebPlugin")
        new("convention", "SerenityConventionPlugin")
        new("resources", "resources.SerenityResourcesPlugin")
        new(null, "SerenityPlugin")
    }
}

dependencies {
    api(plugin(libs.plugins.kotlin.multiplatform))
    api(plugin(libs.plugins.kotlin.plugin.compose))
    api(plugin(libs.plugins.kotlin.plugin.serialization))
    api(plugin(libs.plugins.kotlin.ksp))
    implementation(libs.openhft.zeroAllocationHashing)
    implementation(libs.kotlinx.serialization.json)
}

val generateVersionResource = tasks.register("generateVersionResource") {
    val outFile =
        project.layout.buildDirectory.file("generated-resources/net/derfruhling/serenity/gradle/VERSION")
    val version = project.provider { version.toString() }

    outputs.file(outFile)

    doLast {
        outFile.get().asFile.writeText(version.get())
    }
}

sourceSets.main {
    resources.srcDir(project.layout.buildDirectory.dir("generated-resources"))
}

tasks.processResources {
    dependsOn(generateVersionResource)
}

fun plugin(p: Provider<PluginDependency>): Provider<String> = p.map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}
