package net.derfruhling.serenity.gradle

import net.derfruhling.serenity.gradle.server.SerenityServerPlugin
import net.derfruhling.serenity.gradle.web.SerenityWebPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class SerenityPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply(SerenityBasePlugin::class)
        target.plugins.apply(SerenityServerPlugin::class)
        target.plugins.apply(SerenityWebPlugin::class)
    }
}
