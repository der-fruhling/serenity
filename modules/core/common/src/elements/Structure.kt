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

object HeadContext {
    @Composable
    fun title(text: String) {
        Element("title") { Text(text) }
    }

    @Composable
    @UnescapedTextDanger
    fun inlineScript(javascript: String, async: Boolean = false, defer: Boolean = false) =
        Element(name = "script", update = {
            set(async) { attribute(Attributes.async, it) }
            set(defer) { attribute(Attributes.defer, it) }
        }) {
            Data(javascript)
        }

    @Composable
    fun useScriptDirectly(uri: String, async: Boolean = false, defer: Boolean = false) =
        Element(name = "script", update = {
            set(uri) { attribute(Attributes.src, it) }
            set(async) { attribute(Attributes.async, it) }
            set(defer) { attribute(Attributes.defer, it) }
        })

    @Composable
    fun useScript(
        uri: String,
        async: Boolean = false,
        defer: Boolean = false,
        preload: Boolean = uri.startsWith('/')
    ) {
        val resolver = ResourceResolver.local.current
        val resolvedUrl = remember(resolver, uri) { resolver.getTargetUrl(uri) }

        if (preload) {
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

        if (preload) {
            preloadSetLocal.current?.add(Preload(resolvedUrl, "style"))
        }

        useStylesheetDirectly(resolvedUrl)
    }

    @Composable
    fun useEntrypoint(projectName: String) {
        val page = currentPageLocal.current
        val content = SerialRegistry.encode<SerialPageHolder>(page)
        val scripts = ScriptLocation.local.current

        preloadSetLocal.current?.let { preload ->
            scripts.js?.let { preload.add(Preload("/_/$it", "script")) }
            scripts.wasm?.let { preload.add(Preload("/_/$it", "script")) }
            scripts.wasmBinary?.let { preload.add(Preload("/_/$it", "fetch")) }
        }

        val import = when {
            scripts.wasm == null && scripts.js == null ->
                error($$"$scripts object in manifest contains no references to scripts")

            //language=javascript
            scripts.wasm == null -> """
                (() => {
                    const name = "/_/${scripts.js}";
                    self.__webpack_public_path__ = name;
                    return import(name);
                })()
            """.trimIndent()

            //language=javascript
            scripts.js == null -> """
                (() => {
                    const name = "/_/${scripts.wasm}";
                    self.__webpack_public_path__ = name;
                    return import(name);
                })()
            """.trimIndent()

            //language=javascript
            else -> """
                (() => {
                    const load = (name) => { self.__webpack_public_path__ = name; return import(name); }
                    const loadWasm = () => load("/_/${scripts.wasm}");
                    const loadJs = () => load("/_/${scripts.js}");
                    
                    const useJs = new URLSearchParams(location.hash).has("s-js-only") || document.cookie.includes("s-js-only=error");
                    
                    return ("WebAssembly" in self && !useJs 
                        ? loadWasm().catch(e => {
                            console.error("Error loading WebAssembly, using JS fallback:", e);
                            document.cookie = "s-js-only=error;max-age=${24.hours.inWholeSeconds};samesite=strict";
                            debugger;
                            return loadJs();
                        })
                        : loadJs()).catch(e => {
                            console.error("Failed to load JavaScript:", e);
                            alert("Failed to load JavaScript for this site: " + e);
                            debugger;
                        })
                })()
            """.trimIndent()
        }

        val sourceCode = remember(projectName, content, scripts) {
            //language=javascript
            """
                $import.then(async () => {
                    if(!("ready" in self)) {
                        const {type: hash, ...content} = $content;
                        const entryFn = (await window["$projectName"])[hash];
                        self.ready = true;
                        if(entryFn) {
                            const r = entryFn(content);
                            if(r instanceof Promise) {
                                r.catch(e => {
                                    console.error("Initialization error:", e);
                                })
                            }
                        }
                        else console.warn("Entry function for", hash, "not found", content);
                    }
                })
            """.trimIndent()
        }

        // this code is trusted
        @OptIn(UnescapedTextDanger::class)
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
