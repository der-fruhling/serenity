plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        create("base") {
            id = "net.derfruhling.compose-html.base"
            implementationClass = "net.derfruhling.html.gradle.ComposeHtmlBasePlugin"
        }

        create("server") {
            id = "net.derfruhling.compose-html.server"
            implementationClass = "net.derfruhling.html.gradle.server.ComposeHtmlServerPlugin"
        }

        create("web") {
            id = "net.derfruhling.compose-html.web"
            implementationClass = "net.derfruhling.html.gradle.web.ComposeHtmlWebPlugin"
        }

        create("stylist") {
            id = "net.derfruhling.compose-html.stylist"
            implementationClass = "net.derfruhling.html.gradle.stylist.ComposeHtmlStylePlugin"
        }

        create("main") {
            id = "net.derfruhling.compose-html"
            implementationClass = "net.derfruhling.html.gradle.ComposeHtmlPlugin"
        }

        create("convention") {
            id = "net.derfruhling.compose-html.convention"
            implementationClass = "net.derfruhling.html.gradle.ComposeHtmlConventionPlugin"
        }
    }
}

dependencies {
    api(plugin(libs.plugins.kotlin.multiplatform))
    api(plugin(libs.plugins.kotlin.plugin.compose))
    api(plugin(libs.plugins.kotlin.plugin.serialization))
    api(plugin(libs.plugins.sassBase))
}

fun plugin(p: Provider<PluginDependency>): Provider<String> = p.map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}
