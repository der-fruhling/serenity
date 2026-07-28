@file:HtmlComposable

package net.derfruhling.serenity.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.Text
import net.derfruhling.serenity.annotations.Client
import net.derfruhling.serenity.annotations.HtmlComposable
import net.derfruhling.serenity.event.ClickEvent
import net.derfruhling.serenity.event.ElementPointerEvent
import net.derfruhling.serenity.event.On
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
