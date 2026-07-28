package net.derfruhling.serenity.gradle

import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

internal class SerenityDependencyHandlerImpl(it: KotlinDependencyHandler) : SerenityDependencyHandler,
    KotlinDependencyHandler by it
