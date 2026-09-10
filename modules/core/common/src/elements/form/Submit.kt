package net.derfruhling.serenity.elements.form

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.attribute
import net.derfruhling.serenity.attribute.HtmlAttributes

@Composable
fun Submit(value: String? = null) {
    Element(
        update = {
            attribute(HtmlAttributes.type, "submit")
            attribute(HtmlAttributes.value, value)
        },
        "input"
    )
}
