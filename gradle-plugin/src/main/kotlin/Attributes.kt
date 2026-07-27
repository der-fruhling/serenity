package net.derfruhling.html.gradle

import org.gradle.api.attributes.Attribute

object Attributes {
    val USAGE = Attribute.of("net.derfruhling.compose-html.usage", ComposeHtmlUsage::class.java)
}