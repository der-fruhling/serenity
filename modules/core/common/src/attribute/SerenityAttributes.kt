package net.derfruhling.serenity.attribute

object SerenityAttributes : AbstractAttributeContainer() {
    val keepIfRemoved by name<Boolean>("data-keep-if-removed")
}