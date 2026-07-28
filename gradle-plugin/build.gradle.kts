plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("plugin.serialization") version embeddedKotlinVersion
}

allprojects {
    group = "net.derfruhling.serenity.gradle"
    version = "0.1.0"

    repositories {
        mavenCentral()
        gradlePluginPortal()
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
        new("vendor-resources", "resources.SerenityResourcesPlugin")
        new(null, "SerenityPlugin")
    }
}

dependencies {
    api(plugin(libs.plugins.kotlin.multiplatform))
    api(plugin(libs.plugins.kotlin.plugin.compose))
    api(plugin(libs.plugins.kotlin.plugin.serialization))
    api(plugin(libs.plugins.sassBase))
    implementation(libs.openhft.zeroAllocationHashing)
    implementation(libs.kotlinx.serialization.json)
}

fun plugin(p: Provider<PluginDependency>): Provider<String> = p.map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}
