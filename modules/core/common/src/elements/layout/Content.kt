package net.derfruhling.serenity.elements.layout

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.elements.StyleClasses

@Composable
fun Header(fn: @Composable () -> Unit) {
    Element("header") { fn() }
}

@Composable
@Deprecated("Use Main {} instead", replaceWith = ReplaceWith("Main", "net.derfruhling.serenity.elements.Main"))
fun Content(fn: @Composable () -> Unit) {
    Element("div", update = {
        init { classes.add(StyleClasses.PageContent) }
    }) { fn() }
}

@Composable
fun Footer(fn: @Composable () -> Unit) {
    Element("footer") { fn() }
}
