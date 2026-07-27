package net.derfruhling.html.gradle

import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

internal class ComposeDependencyHandlerImpl(it: KotlinDependencyHandler) : ComposeDependencyHandler,
    KotlinDependencyHandler by it
