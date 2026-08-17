package net.derfruhling.serenity.elements

import net.derfruhling.serenity.attribute.AttributeValue

enum class Rel(override val asValue: String) : AttributeValue {
    EXTERNAL("external"),
    HELP("help"),
    LICENSE("license"),
    NEXT("next"),
    NO_FOLLOW("nofollow"),
    NO_OPENER("noopener"),
    NO_REFERRER("noreferrer"),
    OPENER("opener"),
    PREV("prev"),
    SEARCH("search"),
}