package net.derfruhling.serenity.elements.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.attribute
import net.derfruhling.serenity.attribute.AttributeValue
import net.derfruhling.serenity.attribute.HtmlAttributes
import net.derfruhling.serenity.attribute.Rel
import net.derfruhling.serenity.event.ElementEvent
import net.derfruhling.serenity.event.Handler
import net.derfruhling.serenity.event.On
import net.derfruhling.serenity.event.SubmitEvent

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
    onSubmit: Handler<ElementEvent>? = null,
    fn: @Composable () -> Unit
) {
    val shouldUseFn = onSubmit != null
    val fn: @Composable () -> Unit = remember(shouldUseFn, fn) {
        if (shouldUseFn) (@Composable {
            On(SubmitEvent, onSubmit)
            fn()
        }) else fn
    }

    Element(
        update = {
            attribute(HtmlAttributes.`accept-charset`, acceptCharset)
            attribute(HtmlAttributes.action, action)
            attribute(HtmlAttributes.autocomplete, autocomplete)
            attribute(HtmlAttributes.enctype, encType)
            attribute(HtmlAttributes.method, method)
            attribute(HtmlAttributes.name, name)
            attribute(HtmlAttributes.novalidate, novalidate)
            attribute(HtmlAttributes.rel, rel?.asValue)
            attribute(HtmlAttributes.target, target)
        },
        "form", fn
    )
}
