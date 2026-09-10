package net.derfruhling.serenity.elements.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.Text
import net.derfruhling.serenity.attribute
import net.derfruhling.serenity.attribute.HtmlAttributes
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
                attribute(HtmlAttributes.value, value)
                attribute(HtmlAttributes.label, label)
                attribute(HtmlAttributes.disabled, disabled)
                attribute(HtmlAttributes.selected, selected)
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
                attribute(HtmlAttributes.value, value)
                attribute(HtmlAttributes.label, label, keepNulls = false)
                attribute(HtmlAttributes.disabled, disabled)
                attribute(HtmlAttributes.selected, selected)
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
                attribute(HtmlAttributes.label, label)
                attribute(HtmlAttributes.disabled, disabled)
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
            attribute(HtmlAttributes.name, name)
            attribute(HtmlAttributes.id, id)
            attribute(HtmlAttributes.multiple, multiple)
            attribute(HtmlAttributes.autofocus, autofocus)
            attribute(HtmlAttributes.disabled, disabled)
            attribute(HtmlAttributes.form, form)
        },
        "select"
    ) {
        SelectOptionsImpl.fn()
    }
}
