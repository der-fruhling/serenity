package net.derfruhling.html.gradle

import org.gradle.api.Named

enum class ComposeHtmlUsage : Named {
    RESOURCES;

    override fun getName(): String = name
}