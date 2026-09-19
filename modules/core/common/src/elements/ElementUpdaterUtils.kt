package net.derfruhling.serenity.elements

import androidx.compose.runtime.Updater
import net.derfruhling.serenity.attribute.Attribute
import net.derfruhling.serenity.platform.ElementNode

fun Updater<ElementNode>.apply(classList: ClassList) {
    set(classList) {
        classes.clear()
        classes += classList
    }
}

fun <T : Any> Updater<ElementNode>.attribute(
    attribute: Attribute<T>,
    value: T?,
    keepNulls: Boolean = attribute.keepNulls
) {
    set(value) { attribute(attribute, it, keepNulls) }
}

fun Updater<ElementNode>.attribute(attribute: Attribute<Boolean>, value: Boolean) {
    set(value) { attribute(attribute, it) }
}
