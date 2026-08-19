package net.derfruhling.serenity.elements.form

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.attribute
import net.derfruhling.serenity.attribute.Attributes

@Composable
fun Submit(value: String? = null) {
    Element(
        update = {
            attribute(Attributes.type, "submit")
            attribute(Attributes.value, value)
        },
        "input"
    )
}