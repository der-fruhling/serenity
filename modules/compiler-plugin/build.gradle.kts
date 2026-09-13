@file:OptIn(ExperimentalWasmDsl::class)

import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication
import org.gradle.kotlin.dsl.support.serviceOf
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.targets.wasm.d8.D8EnvSpec
import org.jetbrains.kotlin.gradle.targets.wasm.d8.D8Plugin

plugins {
    kotlin("jvm")
    `java-test-fixtures`
    alias(libs.plugins.node.gradle)
    idea
    id("published")
}

version = "${libs.versions.kotlin.asProvider().get()}-${rootProject.version}"

project.plugins.apply(D8Plugin::class.java)

val testDataDir = layout.projectDirectory.dir("testData")
val testGenDirectory = layout.buildDirectory.dir("test-gen")

repositories {
    maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies") {
        name = "IntelliJ Kotlin Compiler builds"
    }
}

sourceSets {
    main {
        java.setSrcDirs(listOf("src"))
        resources.setSrcDirs(listOf("resources"))
    }

    testFixtures {
        java.setSrcDirs(listOf("test-fixtures"))
    }

    test {
        java.setSrcDirs(listOf("test", testGenDirectory))
        resources.setSrcDirs(listOf(testDataDir))
    }
}

val include = configurations.create("include")

configurations.implementation.configure {
    extendsFrom(include)
}

val kotlinVersionAttribute = Attribute.of("net.derfruhling.kotlin-version", String::class.java)

fun kotlinVersionSourceSet(name: String, targetVersion: String) {
    val sourceSet = sourceSets.create(name) {
        java.setSrcDirs(listOf("src"))
        resources.setSrcDirs(listOf("resources"))

        configurations.named(compileOnlyConfigurationName) {
            extendsFrom(configurations.getByName(sourceSets.main.get().compileClasspathConfigurationName))

            resolutionStrategy {
                dependencySubstitution {
                    substitute(module("org.jetbrains.kotlin:kotlin-compiler:${libs.versions.kotlin}"))
                        .using(module("org.jetbrains.kotlin:kotlin-compiler:${targetVersion}"))
                }
            }
        }

        configurations.named(apiConfigurationName) {
            extendsFrom(configurations.getByName(sourceSets.main.get().apiConfigurationName))
        }
    }

    val component = project.serviceOf<SoftwareComponentFactory>().adhoc("${name}Java")
    components.add(component)

    val baseAttributes = configurations.runtimeElements.get().attributes

    val buildJar = tasks.register(sourceSet.jarTaskName, Jar::class) {
        group = "build"
        description = "Builds the $name variant"

        archiveVersion = provider { "$targetVersion-${rootProject.version}" }

        dependsOn(sourceSet.classesTaskName, sourceSet.processResourcesTaskName)
        from(sourceSet.output, provider { include.resolve().map { zipTree(it) } })
    }

    val runtimeElements = configurations.create("${name}RuntimeElements") {
        attributes {
            @Suppress("UnstableApiUsage")
            addAllLater(baseAttributes)

            attribute(kotlinVersionAttribute, targetVersion)
        }

        outgoing {
            capability("${project.group}:${project.name}:${targetVersion}-${rootProject.version}")
        }
    }

    artifacts.add(runtimeElements.name, buildJar)

    component.addVariantsFromConfiguration(runtimeElements) {
        mapToMavenScope("runtime")
    }
    publishing {
        publications {
            create(name, MavenPublication::class) {
                version = "$targetVersion-${rootProject.version}"
                from(component)

                if(this is DefaultMavenPublication) {
                    isAlias = true
                }
            }
        }
    }
}

kotlinVersionSourceSet("ij262_52", "2.4.20-ij262-52")

idea {
    // This is needed until IDEA fixes IDEA-339729.
    module.generatedSourceDirs.add(testGenDirectory.get().asFile)
}

val testArtifacts: Configuration = configurations.create("testArtifact")

val annotationsRuntimeClasspath = configurations.dependencyScope("annotationsRuntimeClasspath") {
    isTransitive = false
}
val annotationsJvmRuntimeClasspath = configurations.resolvable("annotationsJvmRuntimeClasspath") {
    extendsFrom(annotationsRuntimeClasspath)
}
val annotationsJsRuntimeClasspath = configurations.resolvable("annotationsJsRuntimeClasspath") {
    extendsFrom(annotationsRuntimeClasspath)
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(KotlinUsages.KOTLIN_RUNTIME))
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.js)
    }
}

configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(project(":serenity-compiler-plugin"))
            .using(variant(project(":serenity-compiler-plugin")) {
                capabilities {
                    requireCapability("net.derfruhling.serenity:serenity-compiler-plugin:${project.version}")
                }
            })
    }
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    compileOnly(libs.kotlin.compiler)

    include(libs.openhft.zeroAllocationHashing)

    testFixturesApi(libs.kotlin.test.junit5)
    testFixturesApi(libs.kotlin.test.framework)
    testFixturesApi(libs.kotlin.compiler)
    testFixturesApi(libs.junit.api)
    testFixturesRuntimeOnly(libs.junit.vintageEngine)
    testFixturesRuntimeOnly(libs.junit.engine)

    annotationsRuntimeClasspath(project(":serenity-annotations"))
    annotationsRuntimeClasspath(project(":serenity-core"))
    annotationsRuntimeClasspath(project(":serenity-localization"))

    // Dependencies required to run the internal test framework.
    testArtifacts(libs.kotlin.stdlib)
    testArtifacts(libs.kotlin.stdlib.jdk8)
    testArtifacts(libs.kotlin.reflect)
    testArtifacts(libs.kotlin.test)
    testArtifacts(libs.kotlin.script.runtime)
    testArtifacts(libs.kotlin.annotations.jvm)

    testArtifacts(libs.kotlin.stdlib.js)
    testArtifacts(libs.kotlin.test.js)
}

tasks.test {
    dependsOn(testArtifacts)
    dependsOn(annotationsJvmRuntimeClasspath)
    dependsOn(annotationsJsRuntimeClasspath)

    useJUnitPlatform()
    workingDir = rootDir

    jvmArgs("-Xmx2G")

    systemProperty("annotationsRuntime.jvm.classpath", annotationsJvmRuntimeClasspath.get().asPath)
    systemProperty("annotationsRuntime.js.classpath", annotationsJsRuntimeClasspath.get().asPath)

    // Properties required to run the internal test framework.
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib", "kotlin-stdlib")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib-jdk8", "kotlin-stdlib-jdk8")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-reflect", "kotlin-reflect")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-test", "kotlin-test")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-script-runtime", "kotlin-script-runtime")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-annotations-jvm", "kotlin-annotations-jvm")

    systemProperty("idea.ignore.disabled.plugins", "true")
    systemProperty("idea.home.path", rootDir)

    // Properties required to run JS tests from the internal test framework.
    val d8EnvSpec = project.the<D8EnvSpec>()
    with(d8EnvSpec) { dependsOn(project.d8SetupTaskProvider) }

    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib-js", "kotlin-stdlib-js")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-test-js", "kotlin-test-js")

    systemProperty("javascript.engine.path.V8", d8EnvSpec.executable.get())
    systemProperty("javascript.engine.path.repl", "${layout.projectDirectory.file("repl.js").asFile}")
    systemProperty("kotlin.js.test.root.out.dir", "${layout.buildDirectory.get().asFile}/js-test-output")
}

tasks.jar {
    from(provider { include.resolve().map { zipTree(it) } })
}

kotlin {
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        optIn.add("org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI")
    }
}

val generateTests = tasks.register<JavaExec>("generateTests") {
    inputs.dir(testDataDir)
        .withPropertyName("testData")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(testGenDirectory)
        .withPropertyName("generatedTests")

    classpath = sourceSets.testFixtures.get().runtimeClasspath
    mainClass.set("net.derfruhling.serenity.compiler.MainKt")
    workingDir = rootDir
    args(
        listOf(
            testGenDirectory.get().asFile.absolutePath,
            testDataDir.asFile.absolutePath,
        )
    )
}

tasks.compileTestKotlin {
    dependsOn(generateTests)
}

fun Test.setLibraryProperty(propName: String, jarName: String) {
    val path = testArtifacts.files
        .find { """$jarName-\d.*""".toRegex().matches(it.name) }
        ?.absolutePath
        ?: return
    systemProperty(propName, path)
}

publishing {
    publications {
        create("java", MavenPublication::class) {
            from(components["java"])
        }
    }
}
