package net.derfruhling.serenity.platform

import net.derfruhling.serenity.Name
import net.derfruhling.serenity.annotations.UnescapedTextDanger
import net.derfruhling.serenity.attribute.Attribute
import net.derfruhling.serenity.attribute.HtmlAttributes
import net.derfruhling.serenity.event.Event
import net.derfruhling.serenity.event.EventType
import kotlin.jvm.JvmInline

@JvmInline
@BuilderDsl
value class Builder<N : ComposeNode>(val node: N) {
    inline fun element(fn: Builder<ElementNode>.() -> Unit): ElementNode {
        require(node is NodeWithChildren<*, *>)
        val element = ElementNode()
        Builder(element).fn()
        node.insert(node.children.size, element)
        return element
    }

    inline fun element(name: Name, fn: Builder<ElementNode>.() -> Unit): ElementNode =
        element { node.name = name; fn() }

    inline fun element(name: String, fn: Builder<ElementNode>.() -> Unit): ElementNode =
        element(Name.of(name), fn)

    fun text(content: String): TextNode = TextNode().apply {
        require(node is NodeWithChildren<*, *>)
        textContent = content
        node.insert(node.children.size, this)
    }

    @UnescapedTextDanger
    fun data(content: String): DataNode = DataNode().apply {
        require(node is NodeWithChildren<*, *>)
        textContent = content
        node.insert(node.children.size, this)
    }

    fun <T : Any> attribute(name: Attribute<T>, value: T?) {
        require(node is ElementNode)
        node.attribute(name, value)
    }

    fun attribute(name: Attribute<Boolean>, value: Boolean) {
        require(node is ElementNode)
        node.attribute(name, value)
    }

    fun <T : Event<*>> eventHandler(event: EventType<T>): EventHandlerNode<T> =
        EventHandlerNode<T>().apply {
            require(node is ElementNode)
            type = event
            node.insert(node.children.size, this)
        }

    inline fun page(
        title: String,
        lang: String = "en",
        head: Builder<ElementNode>.() -> Unit = {},
        body: Builder<ElementNode>.() -> Unit
    ): ElementNode =
        element("html") {
            attribute(HtmlAttributes.lang, lang)

            element("head") {
                element("title") {
                    text(title)
                }

                head()
            }

            element("body") {
                body()
            }
        }
}

expect fun getDocumentForTesting(): Document

fun tree(fn: Builder<Document>.() -> Unit): Document {
    val doc = getDocumentForTesting()
    Builder(doc).fn()
    return doc
}
