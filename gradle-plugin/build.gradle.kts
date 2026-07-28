plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("plugin.serialization") version embeddedKotlinVersion
}

allprojects {
    group = "net.derfruhling.html.gradle"
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
                id = "net.derfruhling.compose-html${name?.let { ".$it" } ?: ""}"
                implementationClass = "net.derfruhling.html.gradle.$implClass"
            }
        }

        new("base", "ComposeHtmlBasePlugin")
        new("server", "server.ComposeHtmlServerPlugin")
        new("web", "web.ComposeHtmlWebPlugin")
        new("convention", "ComposeHtmlConventionPlugin")
        new("vendor-resources", "resources.ResourceVendorPlugin")
        new(null, "ComposeHtmlPlugin")
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
