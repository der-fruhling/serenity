package net.derfruhling.serenity.elements.form

import androidx.compose.runtime.*
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.attribute
import net.derfruhling.serenity.attribute.HtmlAttributes
import net.derfruhling.serenity.dom.HTMLInputElement
import net.derfruhling.serenity.elements.StyleClasses
import net.derfruhling.serenity.event.Event
import net.derfruhling.serenity.event.Handler2
import net.derfruhling.serenity.event.InputEvent
import net.derfruhling.serenity.event.On
import net.derfruhling.serenity.event.checkType
import net.derfruhling.serenity.isServerStatic
import net.derfruhling.serenity.platform.ElementNode

enum class TextInputType(val actual: String, val hasSpan: Boolean = false) {
    PLAIN("text"),
    SEARCH("search", hasSpan = true),
}

@Composable
fun TextInput(
    type: TextInputType = TextInputType.PLAIN,
    hasSpan: Boolean = type.hasSpan,
    name: String? = null,
    id: String? = null,
    initialText: String = "",
    placeholder: String? = null,
    onChange: Handler2<Event<HTMLInputElement>, String>
) {
    if(hasSpan) {
        Element(update = {
            setSpanClasses(type)
        }, "span") {
            commonTextInput(type, hasSpan, name, id, initialText, placeholder, onChange)
        }
    } else {
        commonTextInput(type, hasSpan, name, id, initialText, placeholder, onChange)
    }
}

@Composable
fun TextInputState(
    type: TextInputType = TextInputType.PLAIN,
    hasSpan: Boolean = type.hasSpan,
    name: String? = null,
    id: String? = null,
    initialText: String = "",
    placeholder: String? = null
): MutableState<String> {
    val state = remember { mutableStateOf(initialText) }

    if(hasSpan) {
        Element(update = {
            setSpanClasses(type)
        }, "span") {
            commonTextInput(type, hasSpan, name, id, initialText, placeholder) {
                state.value = it
            }
        }
    } else {
        commonTextInput(type, hasSpan, name, id, initialText, placeholder) {
            state.value = it
        }
    }

    return state
}

private fun Updater<ElementNode>.setSpanClasses(type: TextInputType) {
    init {
        classes.add(StyleClasses.TextInput)
    }

    setCommonClasses(type)
}

private fun Updater<ElementNode>.setCommonClasses(type: TextInputType) {
    set(type) {
        setClass(StyleClasses.SearchInput, it == TextInputType.SEARCH)
    }
}

@Composable
private fun commonTextInput(
    type: TextInputType,
    hasSpan: Boolean,
    name: String?,
    id: String?,
    initialText: String,
    placeholder: String?,
    onChange: Handler2<Event<HTMLInputElement>, String>
) {
    Element(update = commonTextInputUpdater(hasSpan, type, name, id, initialText, placeholder), "input") {
        On(InputEvent) {
            checkType<HTMLInputElement>()
            onChange(target.value)
        }
    }
}

private fun commonTextInputUpdater(
    hasSpan: Boolean,
    type: TextInputType,
    name: String?,
    id: String?,
    initialText: String,
    placeholder: String?
): Updater<ElementNode>.() -> Unit = {
    if(!hasSpan) set(hasSpan) {
        setClass(StyleClasses.TextInput, !it)
    }

    setCommonClasses(type)

    attribute(HtmlAttributes.type, type.actual)
    attribute(HtmlAttributes.name, name)
    attribute(HtmlAttributes.id, id)
    attribute(HtmlAttributes.placeholder, placeholder)

    if (isServerStatic && initialText.isNotEmpty()) {
        init(initialText) {
            attribute(HtmlAttributes.value, it)
        }
    }
}
