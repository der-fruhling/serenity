package net.derfruhling.serenity.gradle

import org.gradle.api.Named

enum class SerenityUsage : Named {
    RESOURCES;

    override fun getName(): String = name
}