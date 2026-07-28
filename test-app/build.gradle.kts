import io.freefair.gradle.plugins.sass.SassCompile
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("net.derfruhling.serenity")
    id("net.derfruhling.serenity.stylist-sass")
    id("net.derfruhling.serenity.convention")
    id("net.derfruhling.serenity.vendor-resources")
    id("com.google.devtools.ksp") version "2.3.10"
}

serenity {
    javaVersion = 25
    disableDefaultDependencies = true

    dependencies {
        implementation(rootProject)
        implementation(libs.androidx.compose.runtime)
        implementation(libs.androidx.compose.runtime.saveable)
        implementation(libs.oshai.kotlinLogging)
    }

    testDependencies {
        implementation(kotlin("test"))
    }

    resources {
        prettyJson = true
    }

    server {
        nativeEntryPoint = "net.derfruhling.serenity.testapp.main"

        jvm()
        macosArm64()
        linuxArm64()
        linuxX64()

        dependencies {
            common {
                implementation(project(":serenity-ktor-server"))
            }

            jvm {
                implementation(libs.ktor.server.netty)
                implementation(libs.logback.classic)
            }

            native {
                implementation(libs.ktor.server.cio)
            }
        }

        testDependencies {
            jvm {
                compileOnly(libs.junit.api)
                runtimeOnly(libs.junit.engine)
            }
        }
    }

    web {
        js()
        @OptIn(ExperimentalWasmDsl::class)
        wasmJs()

        dependencies {
            implementation(libs.kotlin.wrappers.js)
            implementation(libs.kotlin.wrappers.browser)
            implementation(libs.kotlin.wrappers.web)
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":serenity-common-collector"))
    add("kspJs", project(":serenity-web-collector"))
    add("kspWasmJs", project(":serenity-web-collector"))
    add("kspLinuxX64", project(":serenity-ktor-collector"))
    add("kspLinuxArm64", project(":serenity-ktor-collector"))
    add("kspMacosArm64", project(":serenity-ktor-collector"))
    add("kspJvm", project(":serenity-ktor-collector"))
}

val resourcesDir = fileTree("src/commonMain/resources/style")
val sassOutDir = layout.buildDirectory.dir("sass")

val compileSass = tasks.register<SassCompile>("compileSass") {
    description = "Compiles stylesheets"

    source(resourcesDir)
    destinationDir.set(sassOutDir)
    group = BasePlugin.BUILD_GROUP
}

afterEvaluate {
    tasks.named { it.startsWith("ksp") && it != "kspAll" && it != "kspCommonMainKotlinMetadata" }.configureEach {
        dependsOn("kspCommonMainKotlinMetadata")
    }

    tasks.register("kspAll") {
        dependsOn(tasks.named { it.startsWith("ksp") && it != "kspAll" })
    }
}

//val resourcesOutPath = layout.buildDirectory.dir("resources")
//val resourcesIn = kotlin.sourceSets.commonMain.map { it.resources }
//
//tasks.processResources {
//    dependsOn(compileSass)
//
//    into(resourcesOutPath)
//
//    from(resourcesIn) {
//        exclude("style/**")
//    }
//
//    into("style") {
//        from(sassOutDir)
//    }
//}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
    enableAssertions = true
}
