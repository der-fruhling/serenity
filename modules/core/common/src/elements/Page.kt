package net.derfruhling.serenity.elements

import androidx.compose.runtime.*
import net.derfruhling.serenity.*
import net.derfruhling.serenity.attribute.HtmlAttributes
import net.derfruhling.serenity.platform.ElementNode

@Composable
@HtmlComposable
fun html(lang: String = "en", content: @Composable HtmlContext.() -> Unit) {
    DocumentType()

    Element(name = "html", update = {
        set(lang) { attribute(HtmlAttributes.lang, it) }
    }) { HtmlContext.content() }
}

val pageTemplateLocal = compositionLocalOf { null as PageTemplate? }
val currentPageLocal = compositionLocalOf<PageHolder<*>> { throw IllegalStateException() }

object PageContext {
    @Composable
    @NonRestartableComposable
    fun Head(fn: @Composable HeadContext.() -> Unit) {
        HtmlContext.head {
            fn()
        }
    }

    @Composable
    inline fun Body(
        noinline updateBody: Updater<ElementNode>.() -> Unit = {},
        crossinline fn: @Composable () -> Unit
    ) {
        HtmlContext.body(updateBody) {
            fn()
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
@Composable
@HtmlComposable
fun Page(
    lang: String = "en",
    body: @Composable PageContext.() -> Unit
) = ReusableContent(lang) {
    html(lang) {
        PageContext.body()
    }
}
