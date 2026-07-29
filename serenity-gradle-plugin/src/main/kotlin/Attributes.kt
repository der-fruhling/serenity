package net.derfruhling.serenity.gradle

import org.gradle.api.attributes.Attribute

object Attributes {
    val USAGE = Attribute.of("net.derfruhling.serenity.usage", SerenityUsage::class.java)
    val STATUS = Attribute.of("net.derfruhling.serenity.status", Status::class.java)
}