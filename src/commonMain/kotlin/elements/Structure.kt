package net.derfruhling.html.elements

import androidx.compose.runtime.Composable
import net.derfruhling.html.Element
import net.derfruhling.html.Text
import net.derfruhling.html.annotations.HtmlComposable
import net.derfruhling.html.attribute.Attributes
import net.derfruhling.html.attribute.ComposableAttribute

object HeadContext {
    @Composable
    @HtmlComposable
    fun title(text: String) {
        Element("title") { Text(text) }
    }

    @Composable
    @HtmlComposable
    fun script(source: String, async: Boolean = false, defer: Boolean = false) =
        Element(name = "script", update = {
            set(source) { attribute(Attributes.src, it) }
            set(async) { attribute(Attributes.async, it) }
            set(defer) { attribute(Attributes.defer, it) }
        })
}

@Composable
@HtmlComposable
expect fun HeadContext.IncludeScript(async: Boolean = false, defer: Boolean = false)

object HtmlContext : ElementContext<HtmlContext.Attr> {
    object Attr : AttributeContext {
        val lang = ComposableAttribute(Attributes.lang)
    }

    override val attributes: Attr
        get() = Attr

    @Composable
    fun head(content: @Composable HeadContext.() -> Unit) =
        Element(name = "head") { HeadContext.content() }

    @Composable
    fun body(content: @Composable () -> Unit) =
        Element(name = "body", content)
}

@Composable
fun html(content: @Composable HtmlContext.() -> Unit) =
    Element(name = "html") { HtmlContext.content() }

@Composable
fun Page(
    title: String,
    lang: String = "en",
    head: @Composable HeadContext.() -> Unit = { IncludeScript() },
    body: @Composable () -> Unit
) {
    html {
        attributes {
            lang { lang }
        }

        head {
            title(title)

            head()
        }

        body { body() }
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class Page(
    val path: String
)
