@file:HtmlComposable

package net.derfruhling.serenity.elements

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.Text
import net.derfruhling.serenity.HtmlComposable
import net.derfruhling.serenity.attribute
import net.derfruhling.serenity.attribute.HtmlAttributes

@Composable
fun Div(fn: @Composable () -> Unit) {
    Element("div") { fn() }
}

@Composable
fun Span(title: String? = null, fn: @Composable () -> Unit) {
    Element(update = {
        attribute(HtmlAttributes.title, title)
    }, "span") { fn() }
}

@Composable
fun Main(fn: @Composable () -> Unit) = Element("main") { fn() }

@Composable
fun Nav(fn: @Composable () -> Unit) = Element("nav") { fn() }

@Composable
fun Paragraph(fn: @Composable () -> Unit) = Element("p") { fn() }

@Composable
fun Bold(fn: @Composable () -> Unit) = Element("b") { fn() }

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun Bold(string: String) = Bold { Text(string.reflow) }

@Composable
fun Italic(fn: @Composable () -> Unit) = Element("b") { fn() }

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun Italic(string: String) = Italic { Text(string.reflow) }

@Composable
fun Underline(fn: @Composable () -> Unit) = Element("u") { fn() }

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun Underline(string: String) = Underline { Text(string.reflow) }

@Composable
fun Strikethrough(fn: @Composable () -> Unit) = Element("s") { fn() }

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun Strikethrough(string: String) = Strikethrough { Text(string.reflow) }

@Composable
fun Deleted(fn: @Composable () -> Unit) = Element("del") { fn() }

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun Deleted(string: String) = Deleted { Text(string.reflow) }
