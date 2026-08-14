plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version embeddedKotlinVersion
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    api(plugin(libs.plugins.kotlin.multiplatform))
    api(plugin(libs.plugins.kotlin.jvm))
    api(plugin(libs.plugins.kotlin.plugin.serialization))
    api(plugin(libs.plugins.kotlin.plugin.compose))
    api(plugin(libs.plugins.stabilityAnalyzer))
}

fun plugin(p: Provider<PluginDependency>): Provider<String> = p.map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}
