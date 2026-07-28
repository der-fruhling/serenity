package net.derfruhling.serenity.gradle.web

import net.derfruhling.serenity.gradle.SerenityBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create

class SerenityWebPlugin : Plugin<Project> {
    lateinit var webExtension: SerenityWebExtension
        private set

    override fun apply(target: Project) {
        val base = target.plugins.apply(SerenityBasePlugin::class)
        webExtension = base.extension.extensions.create("web", SerenityWebExtension::class, base.extension)
    }
}
