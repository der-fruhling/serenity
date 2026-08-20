@file:HtmlComposable

package net.derfruhling.serenity.elements

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.Text
import net.derfruhling.serenity.HtmlComposable

@Composable
fun Div(fn: @Composable () -> Unit) {
    Element("div") { fn() }
}

@Composable
fun Span(fn: @Composable () -> Unit) {
    Element("span") { fn() }
}

@Composable
fun Paragraph(string: String) {
    Element("p") { Text(string.reflow) }
}

@Composable
fun Bold(fn: @Composable () -> Unit) {
    Element("b") { fn() }
}

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun Bold(string: String) {
    Bold { Text(string.reflow) }
}

@Composable
fun Italic(fn: @Composable () -> Unit) {
    Element("b") { fn() }
}

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun Italic(string: String) {
    Italic { Text(string.reflow) }
}
