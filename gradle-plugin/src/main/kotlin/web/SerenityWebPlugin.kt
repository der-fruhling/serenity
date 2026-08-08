package net.derfruhling.serenity.gradle.web

import net.derfruhling.serenity.gradle.SerenityApplicationPlugin
import net.derfruhling.serenity.gradle.SerenityBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.registerIfAbsent

class SerenityWebPlugin : Plugin<Project> {
    lateinit var webExtension: SerenityWebExtension
        private set

    override fun apply(target: Project) {
        val base = target.plugins.apply(SerenityBasePlugin::class)
        target.plugins.apply(SerenityApplicationPlugin::class)
        target.gradle.sharedServices.registerIfAbsent("wasmParser", WasmParserService::class) {
            maxParallelUsages.set(1)
        }
        webExtension =
            base.extension.extensions.create("web", SerenityWebExtension::class, base.extension)
    }
}
