package net.derfruhling.serenity.elements.form

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.attribute.HtmlAttributes
import net.derfruhling.serenity.elements.attribute

@Composable
fun Submit(value: String? = null) {
    Element("input", update = {
        attribute(HtmlAttributes.type, "submit")
        attribute(HtmlAttributes.value, value)
    })
}
