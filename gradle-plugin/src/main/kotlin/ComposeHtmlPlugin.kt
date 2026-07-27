package net.derfruhling.html.gradle

import net.derfruhling.html.gradle.server.ComposeHtmlServerPlugin
import net.derfruhling.html.gradle.stylist.ComposeHtmlStylePlugin
import net.derfruhling.html.gradle.web.ComposeHtmlWebPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class ComposeHtmlPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply(ComposeHtmlBasePlugin::class)
        target.plugins.apply(ComposeHtmlServerPlugin::class)
        target.plugins.apply(ComposeHtmlWebPlugin::class)
        target.plugins.apply(ComposeHtmlStylePlugin::class)
    }
}
