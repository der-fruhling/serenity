@file:HtmlComposable

package net.derfruhling.serenity.elements

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.HtmlComposable
import net.derfruhling.serenity.Text
import net.derfruhling.serenity.attribute
import net.derfruhling.serenity.attribute.HtmlAttributes

@Composable
fun Div(
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    title: String? = null,
    fn: @Composable () -> Unit
) {
    Element(update = {
        apply(classList)
        attribute(HtmlAttributes.id, id)
        attribute(HtmlAttributes.title, title)
    }, "div") { fn() }
}

@Composable
fun Span(
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    title: String? = null,
    fn: @Composable () -> Unit
) {
    Element(update = {
        apply(classList)
        attribute(HtmlAttributes.id, id)
        attribute(HtmlAttributes.title, title)
    }, "span") { fn() }
}

@Composable
fun Main(
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable () -> Unit
) = Element(update = {
    apply(classList)
    attribute(HtmlAttributes.id, id)
}, "main") { fn() }

@Composable
fun Nav(
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable () -> Unit
) = Element(update = {
    apply(classList)
    attribute(HtmlAttributes.id, id)
}, "nav") { fn() }

@Composable
fun Paragraph(
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable () -> Unit
) = Element(update = {
    apply(classList)
    attribute(HtmlAttributes.id, id)
}, "p") { fn() }

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

@Composable
fun Code(
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable () -> Unit
) = Element(update = {
    apply(classList)
    attribute(HtmlAttributes.id, id)
}, "code") { fn() }

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun Code(string: String) = Code { Text(string.reflow) }

@Composable
fun Kbd(
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable () -> Unit
) = Element(update = {
    apply(classList)
    attribute(HtmlAttributes.id, id)
}, "kbd") { fn() }

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun Kbd(string: String) = Kbd { Text(string.reflow) }

@Composable
fun Article(
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable () -> Unit
) = Element(update = {
    apply(classList)
    attribute(HtmlAttributes.id, id)
}, "article") { fn() }
