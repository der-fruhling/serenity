import net.derfruhling.serenity.build.FetchableDataService
import org.gradle.kotlin.dsl.registerIfAbsent

plugins {
    id("net.derfruhling.serenity.base") apply false
    id("net.derfruhling.serenity.web") apply false
    id("net.derfruhling.serenity.server") apply false
    id("net.derfruhling.serenity.stylist-sass") apply false
    id("net.derfruhling.serenity.convention") apply false
    id("net.derfruhling.serenity") apply false
    id("net.derfruhling.serenity.resources") apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.kotest) apply false
}

allprojects {
    gradle.sharedServices.registerIfAbsent("fetchData", FetchableDataService::class) {
        parameters {
            dataLocation = rootProject.layout.buildDirectory.dir("fetched")
        }
    }
}
