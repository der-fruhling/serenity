import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("multiplatform-server")
    kotlin("plugin.serialization")
    kotlin("plugin.compose")
    id("com.github.skydoves.compose.stability.analyzer")
}
