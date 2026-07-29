package net.derfruhling.serenity.gradle

import com.google.devtools.ksp.gradle.KspGradleSubplugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class SerenityApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply(KspGradleSubplugin::class)

        target.afterEvaluate {
            tasks.named { it.startsWith("ksp") && it != "kspAll" && it != "kspCommonMainKotlinMetadata" }
                .configureEach {
                    dependsOn("kspCommonMainKotlinMetadata")
                }

            tasks.register("kspAll") {
                dependsOn(tasks.named { it.startsWith("ksp") && it != "kspAll" })
            }
        }
    }
}
