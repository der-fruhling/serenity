package net.derfruhling.serenity.elements

import androidx.compose.runtime.Updater
import net.derfruhling.serenity.platform.ElementNode

fun Updater<ElementNode>.apply(classList: ClassList) {
    set(classList) {
        classes.clear()
        classes += classList
    }
}
