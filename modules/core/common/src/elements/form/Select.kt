package net.derfruhling.serenity.elements.form

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.Text
import net.derfruhling.serenity.attribute
import net.derfruhling.serenity.attribute.Attributes

sealed class SelectOptions {
    @Composable
    fun Option(
        value: String,
        label: String? = null,
        disabled: Boolean = false,
        selected: Boolean = false,
        fn: @Composable () -> Unit
    ) {
        Element(
            update = {
                attribute(Attributes.value, value)
                attribute(Attributes.label, label)
                attribute(Attributes.disabled, disabled)
                attribute(Attributes.selected, selected)
            },
            "option", fn
        )
    }

    @Composable
    fun Option(
        value: String,
        label: String? = null,
        disabled: Boolean = false,
        selected: Boolean = false,
        text: String,
        fn: (@Composable () -> Unit)? = null
    ) {
        Element(
            update = {
                attribute(Attributes.value, value)
                attribute(Attributes.label, label)
                attribute(Attributes.disabled, disabled)
                attribute(Attributes.selected, selected)
            },
            "option"
        ) {
            Text(text)
            fn?.invoke()
        }
    }
}

sealed class SelectOptionsRoot : SelectOptions() {
    @Composable
    fun Group(
        label: String? = null,
        disabled: Boolean = false,
        fn: @Composable SelectOptions.() -> Unit
    ) {
        Element(
            update = {
                attribute(Attributes.label, label)
                attribute(Attributes.disabled, disabled)
            },
            "optgroup"
        ) { fn() }
    }
}

private object SelectOptionsImpl : SelectOptionsRoot()

@Composable
fun Select(
    name: String? = null,
    id: String? = null,
    multiple: Boolean = false,
    autofocus: Boolean = false,
    disabled: Boolean = false,
    form: String? = null,
    fn: @Composable SelectOptions.() -> Unit
) {
    Element(
        update = {
            attribute(Attributes.name, name)
            attribute(Attributes.id, id)
            attribute(Attributes.multiple, multiple)
            attribute(Attributes.autofocus, autofocus)
            attribute(Attributes.disabled, disabled)
            attribute(Attributes.form, form)
        },
        "select"
    ) {
        SelectOptionsImpl.fn()
    }
}