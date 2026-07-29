package net.derfruhling.serenity.gradle

import org.gradle.api.Named

enum class SerenityUsage(val actualName: String) : Named, Comparable<SerenityUsage> {
    RESOURCES_ZIP("resources-zip"),
    RESOURCES_DIR("resources-dir"),

    ;

    override fun getName(): String = actualName
}