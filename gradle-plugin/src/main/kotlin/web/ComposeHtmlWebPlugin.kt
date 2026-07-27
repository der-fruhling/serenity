package net.derfruhling.html.gradle.web

import net.derfruhling.html.gradle.ComposeHtmlBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create

class ComposeHtmlWebPlugin : Plugin<Project> {
    lateinit var webExtension: ComposeHtmlWebExtension
        private set

    override fun apply(target: Project) {
        val base = target.plugins.apply(ComposeHtmlBasePlugin::class)
        webExtension = base.extension.extensions.create("web", ComposeHtmlWebExtension::class, base.extension)
    }
}
