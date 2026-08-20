import net.derfruhling.serenity.build.FetchBrowserCompatData
import net.derfruhling.serenity.build.GenerateStyles

plugins {
    id("multiplatform-compose")
    id("net.derfruhling.serenity.convention")
    id("published")
}

allprojects {
    group = "net.derfruhling.serenity"

    apply(from = rootProject.file("common.gradle.kts"))
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":serenity-core"))
            }
        }

        commonTest {
            dependencies {
                implementation(project(":serenity-test"))
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

val fetchBrowserCompatData = tasks.register<FetchBrowserCompatData>("fetchBrowserCompatData") {
    description = "Fetches the browser compat data repo using git"
    group = "other"

    targetDir = project.layout.buildDirectory.dir("fetch/browser-compat-data")
}

tasks.register<GenerateStyles>("generateStyleRules") {
    description = "Generates Kotlin sources for each rule defined in rules.xml"
    group = "build"

    dependsOn(fetchBrowserCompatData)

    sourceRules = file("common/src/rules.xml")
    output = project.layout.buildDirectory.dir("rules")
    browserCompatData = project.layout.buildDirectory.dir("fetch/browser-compat-data")
}
