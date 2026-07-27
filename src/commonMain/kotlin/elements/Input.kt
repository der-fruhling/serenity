@file:HtmlComposable

package net.derfruhling.html.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import net.derfruhling.html.Element
import net.derfruhling.html.Text
import net.derfruhling.html.annotations.Client
import net.derfruhling.html.annotations.HtmlComposable
import net.derfruhling.html.event.ClickEvent
import net.derfruhling.html.event.ElementPointerEvent
import net.derfruhling.html.event.On
import kotlin.jvm.JvmName

object Button : GeneralStructure<Button>(), ElementContext<Button.Attr> {
    override val attributes: Attr
        get() = Attr

    object Attr : GeneralStructure.Attr()
}

@Composable
fun Button(fn: @Composable Button.() -> Unit) {
    Element("button") {
        Button.fn()
    }
}

@Composable
@JvmName("ButtonLabeled")
fun Button(label: String, fn: @Composable Button.() -> Unit = {}) {
    Button {
        Text(label.reflow)
        fn()
    }
}

@Composable
@JvmName("ButtonWithEvent")
fun Button(
    label: String,
    onClick: @Client (ElementPointerEvent) -> Unit,
    fn: @Composable Button.() -> Unit = {}
) {
    Button {
        Text(label.reflow)
        On(ClickEvent) { onClick(it) }
        fn()
    }
}
