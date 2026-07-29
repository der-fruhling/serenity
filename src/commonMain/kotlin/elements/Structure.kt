package net.derfruhling.serenity.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.Updater
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.PageHolder
import net.derfruhling.serenity.Text
import net.derfruhling.serenity.attribute.Attributes
import net.derfruhling.serenity.manifest.Preload
import net.derfruhling.serenity.manifest.ResourceResolver
import net.derfruhling.serenity.manifest.preloadSetLocal
import net.derfruhling.serenity.tree.platform.ElementNode

object HeadContext {
    @Composable
    fun title(text: String) {
        Element("title") { Text(text) }
    }

    @Composable
    fun inlineScript(javascript: String, async: Boolean = false, defer: Boolean = false) =
        Element(name = "script", update = {
            set(async) { attribute(Attributes.async, it) }
            set(defer) { attribute(Attributes.defer, it) }
        }) {
            Text(javascript)
        }

    @Composable
    fun useScriptDirectly(uri: String, async: Boolean = false, defer: Boolean = false) =
        Element(name = "script", update = {
            set(uri) { attribute(Attributes.src, it) }
            set(async) { attribute(Attributes.async, it) }
            set(defer) { attribute(Attributes.defer, it) }
        })

    @Composable
    fun useScript(uri: String, async: Boolean = false, defer: Boolean = false, preload: Boolean = uri.startsWith('/')) {
        val resolver = ResourceResolver.local.current
        val resolvedUrl = remember(resolver, uri) { resolver.getTargetUrl(uri) }

        if(preload) {
            preloadSetLocal.current?.add(Preload(resolvedUrl, "script"))
        }

        useScriptDirectly(resolvedUrl, async, defer)
    }

    @Composable
    fun link(rel: String, href: String) {
        Element(name = "link", update = {
            set(rel) { attribute(Attributes.rel, it) }
            set(href) { attribute(Attributes.href, it) }
        })
    }

    @Composable
    fun useStylesheetDirectly(uri: String) =
        link("stylesheet", uri)

    @Composable
    fun useStylesheet(uri: String, preload: Boolean = uri.startsWith('/')) {
        val resolver = ResourceResolver.local.current
        val resolvedUrl = remember(resolver, uri) { resolver.getTargetUrl(uri) }

        if(preload) {
            preloadSetLocal.current?.add(Preload(resolvedUrl, "style"))
        }

        useStylesheetDirectly(resolvedUrl)
    }

    @Composable
    fun useEntrypoint(projectName: String) {
        val hash = currentPageLocal.current.id

        val sourceCode = remember(projectName, hash) {
            //language=javascript
            """
                if(!("ready" in self)) {
                    const entryFn = window["$projectName"]["$hash"];
                    self.ready = true;
                    if(entryFn) entryFn();
                    else console.warn("Entry function for", "$hash", "not found");
                }
            """.trimIndent()
        }

        inlineScript(sourceCode)
    }
}

object HtmlContext {
    @Composable
    fun head(content: @Composable HeadContext.() -> Unit) =
        Element(name = "head") { HeadContext.content() }

    @Composable
    fun body(updateBody: Updater<ElementNode>.() -> Unit = {}, content: @Composable () -> Unit) =
        Element(updateBody, name = "body") { content() }
}

@Composable
fun html(lang: String = "en", content: @Composable HtmlContext.() -> Unit) =
    Element(name = "html", update = {
        set(lang) { attribute(Attributes.lang, it) }
    }) { HtmlContext.content() }

val headBuilderLocal = compositionLocalOf { null as (@Composable HeadContext.() -> Unit)? }
val currentPageLocal = compositionLocalOf<PageHolder> { throw IllegalStateException() }

@Composable
fun Page(
    title: String,
    lang: String = "en",
    head: @Composable HeadContext.() -> Unit = {},
    updateBody: @DisallowComposableCalls Updater<ElementNode>.() -> Unit = {},
    body: @Composable () -> Unit
) {
    html(lang) {
        head {
            title(title)

            val headBuilder = headBuilderLocal.current
            if(headBuilder != null) {
                headBuilder()
            }

            head()
        }

        body(updateBody) { body() }
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class Page(
    val path: String
)
