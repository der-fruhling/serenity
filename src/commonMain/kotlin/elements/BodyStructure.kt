@file:HtmlComposable

package net.derfruhling.html.elements

import androidx.compose.runtime.Composable
import net.derfruhling.html.Element
import net.derfruhling.html.Text
import net.derfruhling.html.annotations.HtmlComposable

object Div : GeneralStructure(), ElementContext<Div.Attr> {
    override val attributes: Attr
        get() = Attr

    object Attr : GeneralStructure.Attr()
}

@Composable
fun Div(fn: @Composable Div.() -> Unit) {
    Element("div") { Div.fn() }
}

@Composable
fun Div(vararg classes: String, fn: @Composable Div.() -> Unit = {}) {
    Element("div") {
        with(Div) {
            attributes.classes(*classes)

            fn()
        }
    }
}

object SpanContext : GeneralStructure(), ElementContext<SpanContext.Attr> {
    override val attributes: Attr
        get() = Attr

    object Attr : GeneralStructure.Attr()
}

@Composable
fun Span(fn: @Composable SpanContext.() -> Unit) {
    Element("span") { SpanContext.fn() }
}

@Composable
fun Span(vararg classes: String, fn: @Composable SpanContext.() -> Unit = {}) {
    Element("span") {
        with(SpanContext) {
            attributes.classes(*classes)

            fn()
        }
    }
}

object Paragraph : GeneralStructure(), ElementContext<Paragraph.Attr> {
    override val attributes: Attr
        get() = Attr

    object Attr : GeneralStructure.Attr()
}

@Composable
fun Paragraph(fn: @Composable Paragraph.() -> Unit) {
    Element("p") { Paragraph.fn() }
}

@Suppress("NOTHING_TO_INLINE")
@Composable
fun Paragraph(string: String) {
    Paragraph { Text(string.reflow) }
}

object Bold : GeneralStructure(), ElementContext<Bold.Attr> {
    override val attributes: Attr
        get() = Attr

    object Attr : GeneralStructure.Attr()
}

@Composable
fun Bold(fn: @Composable Bold.() -> Unit) {
    Element("b") { Bold.fn() }
}

@Suppress("NOTHING_TO_INLINE")
@Composable
fun Bold(string: String) {
    Bold { Text(string.reflow) }
}

object Italic : GeneralStructure(), ElementContext<Italic.Attr> {
    override val attributes: Attr
        get() = Attr

    object Attr : GeneralStructure.Attr()
}

@Composable
fun Italic(fn: @Composable Italic.() -> Unit) {
    Element("b") { Italic.fn() }
}

@Suppress("NOTHING_TO_INLINE")
@Composable
fun Italic(string: String) {
    Italic { Text(string.reflow) }
}
