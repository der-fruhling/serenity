package net.derfruhling.serenity.gradle

import org.gradle.api.Named

enum class SerenityUsage : Named, Comparable<SerenityUsage> {
    RESOURCES_ZIP,
    RESOURCES_DIR,

    ;

    override fun getName(): String = name
}
