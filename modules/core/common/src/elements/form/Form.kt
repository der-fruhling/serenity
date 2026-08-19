package net.derfruhling.serenity.elements.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.annotations.Client
import net.derfruhling.serenity.attribute
import net.derfruhling.serenity.attribute.AttributeValue
import net.derfruhling.serenity.attribute.Attributes
import net.derfruhling.serenity.dom.Element
import net.derfruhling.serenity.attribute.Rel
import net.derfruhling.serenity.event.Event
import net.derfruhling.serenity.event.EventContext
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
    onSubmit: (@Client EventContext.(Event<Element>) -> Unit)? = null,
    fn: @Composable () -> Unit
) {
    val shouldUseFn = onSubmit != null
    val fn: @Composable () -> Unit = remember(shouldUseFn, fn) {
        if (shouldUseFn) (@Composable {
            On(SubmitEvent, onSubmit)
        }) else fn
    }

    Element(
        update = {
            attribute(Attributes.`accept-charset`, acceptCharset)
            attribute(Attributes.action, action)
            attribute(Attributes.autocomplete, autocomplete)
            attribute(Attributes.enctype, encType)
            attribute(Attributes.method, method)
            attribute(Attributes.name, name)
            attribute(Attributes.novalidate, novalidate)
            attribute(Attributes.rel, rel?.asValue)
            attribute(Attributes.target, target)
        },
        "form", fn
    )
}