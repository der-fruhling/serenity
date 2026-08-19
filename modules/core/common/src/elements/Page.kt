package net.derfruhling.serenity.elements

import androidx.compose.runtime.*
import net.derfruhling.serenity.*
import net.derfruhling.serenity.annotations.UnescapedTextDanger
import net.derfruhling.serenity.attribute.Attributes
import net.derfruhling.serenity.manifest.Preload
import net.derfruhling.serenity.manifest.ResourceResolver
import net.derfruhling.serenity.manifest.ScriptLocation
import net.derfruhling.serenity.manifest.preloadSetLocal
import net.derfruhling.serenity.tree.platform.ElementNode
import kotlin.time.Duration.Companion.hours

@Composable
fun html(lang: String = "en", content: @Composable HtmlContext.() -> Unit) {
    DocumentType()

    Element(name = "html", update = {
        set(lang) { attribute(Attributes.lang, it) }
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

    @Composable
    @NonRestartableComposable
    inline fun Layout(
        crossinline updateBody: Updater<ElementNode>.() -> Unit = {},
        crossinline fn: @Composable () -> Unit
    ) {
        Body(updateBody = {
            init { classes.add(StyleClasses.PageLayout) }
            updateBody()
        }) { fn() }
    }
}

@Suppress("NOTHING_TO_INLINE")
@Composable
fun Page(
    lang: String = "en",
    body: @Composable PageContext.() -> Unit
) = ReusableContent(lang) {
    html(lang) {
        PageContext.body()
    }
}
