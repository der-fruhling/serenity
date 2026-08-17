@file:HtmlComposable

package net.derfruhling.serenity.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.Text
import net.derfruhling.serenity.annotations.Client
import net.derfruhling.serenity.annotations.HtmlComposable
import net.derfruhling.serenity.attribute
import net.derfruhling.serenity.attribute.AttributeValue
import net.derfruhling.serenity.attribute.Attributes
import net.derfruhling.serenity.dom.Element
import net.derfruhling.serenity.event.ClickEvent
import net.derfruhling.serenity.event.ElementPointerEvent
import net.derfruhling.serenity.event.Event
import net.derfruhling.serenity.event.EventContext
import net.derfruhling.serenity.event.On
import net.derfruhling.serenity.event.SubmitEvent
import kotlin.jvm.JvmName

@Immutable
enum class FormEncoding(override val asValue: String) : AttributeValue {
    URL_ENCODED("application/x-www-form-urlencoded"),
    MULTIPART("multipart/form-data"),
    PLAIN_TEXT("text/plain")
}

@Immutable
enum class FormMethod(override val asValue: String) : AttributeValue {
    DIALOG("dialog"),
    GET("get"),
    POST("post")
}

@Composable
fun Form(
    acceptCharset: String = "UTF-8",
    action: String? = null,
    autocomplete: Boolean = false,
    encType: FormEncoding? = null,
    method: FormMethod? = null,
    name: String? = null,
    novalidate: Boolean = false,
    rel: Rel? = null,
    target: String? = null,
    onSubmit: (@Client EventContext.(Event<Element>) -> Unit)? = null,
    fn: @Composable () -> Unit
) {
    val shouldUseFn = onSubmit != null
    val fn: @Composable () -> Unit = remember(shouldUseFn, fn) {
        if(shouldUseFn) (@Composable {
            On(SubmitEvent, onSubmit)
        }) else fn
    }

    Element(update = {
        attribute(Attributes.`accept-charset`, acceptCharset)
        attribute(Attributes.action, action)
        attribute(Attributes.autocomplete, autocomplete)
        attribute(Attributes.enctype, encType)
        attribute(Attributes.method, method)
        attribute(Attributes.name, name)
        attribute(Attributes.novalidate, novalidate)
        attribute(Attributes.rel, rel?.asValue)
        attribute(Attributes.target, target)
    }, "form", fn)
}

@Composable
fun Button(
    label: String? = null,
    onClick: (@Client EventContext.(ElementPointerEvent) -> Unit)? = null,
    fn: @Composable () -> Unit = {}
) {
    Element("button") {
        label?.let { Text(it.reflow) }
        if(onClick != null) On(ClickEvent, onClick)
        fn()
    }
}

@Composable
fun Submit(value: String? = null) {
    Element(update = {
        attribute(Attributes.type, "submit")
        attribute(Attributes.value, value)
    }, "input")
}

sealed class SelectOptions {
    @Composable
    fun Option(
        value: String,
        label: String? = null,
        disabled: Boolean = false,
        selected: Boolean = false,
        fn: @Composable () -> Unit
    ) {
        Element(update = {
            attribute(Attributes.value, value)
            attribute(Attributes.label, label)
            attribute(Attributes.disabled, disabled)
            attribute(Attributes.selected, selected)
        }, "option", fn)
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
        Element(update = {
            attribute(Attributes.value, value)
            attribute(Attributes.label, label)
            attribute(Attributes.disabled, disabled)
            attribute(Attributes.selected, selected)
        }, "option") {
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
        Element(update = {
            attribute(Attributes.label, label)
            attribute(Attributes.disabled, disabled)
        }, "optgroup") { fn() }
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
    Element(update = {
        attribute(Attributes.name, name)
        attribute(Attributes.id, id)
        attribute(Attributes.multiple, multiple)
        attribute(Attributes.autofocus, autofocus)
        attribute(Attributes.disabled, disabled)
        attribute(Attributes.form, form)
    }, "select") {
        SelectOptionsImpl.fn()
    }
}
