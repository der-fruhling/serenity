@file:HtmlComposable

package net.derfruhling.serenity.elements

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.Text
import net.derfruhling.serenity.annotations.HtmlComposable

object Div : GeneralStructure<Div>(), ElementContext<Div.Attr> {
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

object Span : GeneralStructure<Span>(), ElementContext<Span.Attr> {
    override val attributes: Attr
        get() = Attr

    object Attr : GeneralStructure.Attr()
}

@Composable
fun Span(fn: @Composable Span.() -> Unit) {
    Element("span") { Span.fn() }
}

@Composable
fun Span(vararg classes: String, fn: @Composable Span.() -> Unit = {}) {
    Element("span") {
        with(Span) {
            attributes.classes(*classes)

            fn()
        }
    }
}

object Paragraph : GeneralStructure<Paragraph>(), ElementContext<Paragraph.Attr> {
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

object Bold : GeneralStructure<Bold>(), ElementContext<Bold.Attr> {
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

object Italic : GeneralStructure<Italic>(), ElementContext<Italic.Attr> {
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
