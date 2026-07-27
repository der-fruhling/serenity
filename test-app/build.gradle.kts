import io.freefair.gradle.plugins.sass.SassCompile
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("net.derfruhling.compose-html")
    id("net.derfruhling.compose-html.convention")
    id("com.google.devtools.ksp") version "2.3.10"
}

ksp {
    arg("net.derfruhling.compose-html.package", "net.derfruhling.html.testapp")
}

composeHtml {
    javaVersion = 25
    disableDefaultDependencies = true

    dependencies {
        implementation(rootProject)
        implementation(libs.androidx.compose.runtime)
        implementation(libs.androidx.compose.runtime.saveable)
        implementation(libs.kermit)
    }

    testDependencies {
        implementation(kotlin("test"))
    }

    server {
        nativeEntryPoint = "net.derfruhling.html.testapp.main"

        jvm()
        macosArm64()
        linuxArm64()
        linuxX64()

        dependencies {
            common {
                implementation(project(":compose-html-ktor-server"))
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
    add("kspCommonMainMetadata", project(":compose-html-common-collector"))
    add("kspJs", project(":compose-html-web-collector"))
    add("kspWasmJs", project(":compose-html-web-collector"))
    add("kspLinuxX64", project(":compose-html-ktor-collector"))
    add("kspLinuxArm64", project(":compose-html-ktor-collector"))
    add("kspMacosArm64", project(":compose-html-ktor-collector"))
    add("kspJvm", project(":compose-html-ktor-collector"))
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
