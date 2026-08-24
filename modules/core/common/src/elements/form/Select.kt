package net.derfruhling.serenity.elements.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.Text
import net.derfruhling.serenity.attribute
import net.derfruhling.serenity.attribute.Attributes
import net.derfruhling.serenity.dom.HTMLSelectElement
import net.derfruhling.serenity.event.*

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
                attribute(Attributes.label, label, keepNulls = false)
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
    onChange: Handler2<Event<HTMLSelectElement>, String>? = null,
    fn: @Composable SelectOptionsRoot.() -> Unit
) {
    val useBody = remember(onChange) { onChange != null }
    val fn = remember(useBody) {
        if (useBody) (@Composable {
            if(onChange != null) {
                On(ChangeEvent) {
                    checkType<HTMLSelectElement>()
                    onChange(target.value)
                }
            }

            fn()
        }) else fn
    }

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
