package net.derfruhling.serenity.elements.form

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.Text
import net.derfruhling.serenity.annotations.Client
import net.derfruhling.serenity.elements.reflow
import net.derfruhling.serenity.event.ClickEvent
import net.derfruhling.serenity.event.ElementPointerEvent
import net.derfruhling.serenity.event.EventContext
import net.derfruhling.serenity.event.On

@Composable
fun Button(
    label: String? = null,
    onClick: (@Client EventContext.(ElementPointerEvent) -> Unit)? = null,
    fn: @Composable () -> Unit = {}
) {
    Element("button") {
        label?.let { Text(it.reflow) }
        if (onClick != null) On(ClickEvent, onClick)
        fn()
    }
}