package net.derfruhling.serenity.tree

import net.derfruhling.serenity.Name
import net.derfruhling.serenity.attribute.Attribute
import net.derfruhling.serenity.attribute.AttributeMap
import net.derfruhling.serenity.attribute.Attributes
import net.derfruhling.serenity.tree.platform.AttributeNode
import net.derfruhling.serenity.tree.platform.ComposeNode
import net.derfruhling.serenity.tree.platform.Document
import net.derfruhling.serenity.tree.platform.ElementNode
import net.derfruhling.serenity.tree.platform.NodeWithChildren
import net.derfruhling.serenity.tree.platform.TextNode
import kotlin.jvm.JvmInline

@JvmInline
@BuilderDsl
value class Builder<N : ComposeNode>(val node: N) {
    inline fun element(fn: Builder<ElementNode>.() -> Unit): ElementNode {
        require(node is NodeWithChildren<*>)
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
        require(node is NodeWithChildren<*>)
        textContent = content
        node.insert(node.children.size, this)
    }

    fun <T : Any> attribute(name: Attribute<T>, value: T?) {
        require(node is ElementNode)
        val newNode = AttributeNode<T>()
        newNode.parser = name
        newNode.value = value
        node.insert(node.children.size, newNode)
    }

    inline fun page(title: String, lang: String = "en", head: Builder<ElementNode>.() -> Unit = {}, body: Builder<ElementNode>.() -> Unit): ElementNode =
        element("html") {
            attribute(Attributes.lang, lang)

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
