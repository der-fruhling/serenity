plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("plugin.serialization") version embeddedKotlinVersion
}

gradlePlugin {
    plugins {
        fun new(name: String?, implClass: String) {
            create(name ?: "main") {
                id = "net.derfruhling.serenity${name?.let { ".$it" } ?: ""}"
                implementationClass = "net.derfruhling.serenity.gradle.$implClass"
            }
        }

        new("stylist-sass", "stylist.SerenitySassPlugin")
    }
}

dependencies {
    api(rootProject)
    api(plugin(libs.plugins.sassBase))
}

fun plugin(p: Provider<PluginDependency>): Provider<String> = p.map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}
