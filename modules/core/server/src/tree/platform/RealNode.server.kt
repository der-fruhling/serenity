@file:Suppress("DEPRECATION")

package net.derfruhling.serenity.tree.platform

import com.fleeksoft.ksoup.nodes.*
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.TextNode
import com.fleeksoft.ksoup.nodes.DataNode
import com.fleeksoft.ksoup.parser.Parser
import net.derfruhling.serenity.Name
import net.derfruhling.serenity.event.EventSubscriptionHandle
import net.derfruhling.serenity.event.EventType

actual fun RealNode(base: UnderlyingBase): RealNode? {
    return when (base) {
        is Document -> RealDocument(base)
        is Element -> RealElement(base)
        is Attribute -> RealAttribute(base)
        is TextNode -> RealText(base)
        is DataNode -> RealData(base)
        is Comment -> RealComment(base)
        is DocumentType -> RealDocumentType(base)
        is Node -> null
        else -> throw IllegalArgumentException("Not a node")
    }
}

actual class RealText actual constructor(actual override val node: UnderlyingText) : RealNode {
    actual var textContent: String
        get() = node.text()
        set(value) {
            node.text(value)
        }

    actual constructor() : this(TextNode(""))
}

actual class RealData actual constructor(actual override val node: UnderlyingData) : RealNode {
    actual var textContent: String
        get() = node.getWholeData()
        set(value) {
            node.setWholeData(value)
        }

    actual constructor() : this(DataNode(""))
}

actual class RealComment actual constructor(actual override val node: UnderlyingComment) : RealNode {
    actual var commentContent: String
        get() = node.getData()
        set(value) {
            node.setData(value)
        }

    actual constructor() : this(Comment(""))
}

actual class RealDocumentType actual constructor(actual override val node: UnderlyingDocType) :
    RealNode {
    actual val type: String = node.name()
    actual val public: String = node.publicId()
    actual val system: String = node.systemId()

    actual constructor(from: RealDocumentType)
        : this(from.type, from.public, from.system)

    actual constructor(type: String, public: String, system: String)
        : this(DocumentType(type, public, system))
}

actual sealed class RealElementLike : RealNode {
    internal abstract val element: Element

    actual abstract val attributeSet: MutableSet<RealAttribute>

    private inner class ChildList : AbstractMutableList<RealNode>() {
        private val realNodes = element.childNodes.map { RealNode(it) }.toMutableList()

        override fun add(index: Int, element: RealNode) {
            this@RealElementLike.element.insertChildren(index, element.node as Node)
            realNodes.add(index, element)
        }

        override fun removeAt(index: Int): RealNode {
            this@RealElementLike.element.child(index).remove()
            return realNodes[index] ?: RealUnknown
        }

        override fun set(
            index: Int,
            element: RealNode
        ): RealNode {
            this@RealElementLike.element.replaceChild(
                this@RealElementLike.element.child(index),
                element.node as Node
            )
            return realNodes.set(index, element) ?: RealUnknown
        }

        override fun get(index: Int): RealNode {
            return realNodes[index] ?: RealUnknown
        }

        override val size: Int
            get() = realNodes.size
    }

    actual val children: MutableList<RealNode> by lazy { ChildList() }
}

actual class RealElement actual constructor(node: UnderlyingElement) :
    RealElementLike() {
    actual override var node = node
        internal set

    actual val name: Name = if (node.tag.prefix() != "") {
        when (node.tag.namespace) {
            "" -> Name.qualified(node.tag.prefix(), node.tag.localName())
            else -> Name.qualified(node.tag.prefix(), node.tag.namespace, node.tag.localName())
        }
    } else {
        when (node.tag.namespace) {
            "" -> Name.of(node.tag.localName())
            else -> Name.of(node.tag.namespace, node.tag.localName())
        }
    }

    inner class AttributeSet : AbstractMutableSet<RealAttribute>() {
        private val map = node.attributes().associate { node ->
            RealAttribute(node).let { it.name to it }
        }.toMutableMap()

        override fun add(element: RealAttribute): Boolean {
            map[element.name] = element
            node.attr((element.name.namespace?.let { "$it:" } ?: "") + element.name.localName,
                      element.value)
            return true
        }

        override fun iterator(): MutableIterator<RealAttribute> {
            return object : MutableIterator<RealAttribute> {
                private val iterator = map.iterator()
                private lateinit var current: RealAttribute

                override fun remove() {
                    iterator.remove()
                    node.attributes()
                        .remove((current.name.namespace?.let { "$it:" }
                            ?: "") + current.name.localName)
                }

                override fun hasNext(): Boolean {
                    return iterator.hasNext()
                }

                override fun next(): RealAttribute {
                    return iterator.next().value.also { current = it }
                }
            }
        }

        override val size: Int
            get() = map.size
    }

    override val element: Element
        get() = node

    actual override val attributeSet: MutableSet<RealAttribute> by lazy { AttributeSet() }

    actual constructor(name: Name) : this(
        if (name.namespaceUrl != null) {
            Element((name.namespace?.let { "$it:" } ?: "") + name.localName, name.namespaceUrl!!)
        } else {
            Element((name.namespace?.let { "$it:" } ?: "") + name.localName)
        })

    actual inline fun <T> subscribe(
        type: EventType<T>,
        crossinline handler: (T) -> Unit
    ): EventSubscriptionHandle {
        return EventSubscriptionHandle.NoOp
    }
}

actual class RealDocument actual constructor(actual override val node: UnderlyingDocument) :
    RealElementLike() {
    override val element: Element
        get() = node

    object UnsupportedAttributeSet : AbstractMutableSet<RealAttribute>() {
        override fun add(element: RealAttribute): Boolean {
            throw UnsupportedOperationException()
        }

        override fun iterator(): MutableIterator<RealAttribute> {
            return object : MutableIterator<RealAttribute> {
                override fun remove() {
                    throw UnsupportedOperationException()
                }

                override fun next(): RealAttribute {
                    throw UnsupportedOperationException()
                }

                override fun hasNext(): Boolean {
                    return false
                }
            }
        }

        override fun clear() {}

        override val size: Int
            get() = 0
    }

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated(
        message = "Attributes not supported on document objects",
        level = DeprecationLevel.ERROR
    )
    actual override val attributeSet: MutableSet<RealAttribute>
        get() = UnsupportedAttributeSet

    actual fun applyFragment(realDocumentFragment: RealDocumentFragment) {
        for (child in realDocumentFragment.children) {
            val base = when (val node = child.node) {
                is Node -> node.clone() as UnderlyingBase
                is Attribute -> node.clone() as UnderlyingBase
                else -> error("whut")
            }
            children.add(RealNode(base)!!)
        }
    }

    constructor(baseUri: String) : this(Document(baseUri))
}

@Deprecated("Avoid if possible")
actual class RealAttribute actual constructor(actual override val node: UnderlyingAttribute) :
    RealNode {
    actual val name: Name
        get() = if (node.prefix() != "") {
            when (node.namespace()) {
                "" -> Name.qualified(node.prefix(), node.localName())
                else -> Name.qualified(node.prefix(), node.namespace(), node.localName())
            }
        } else {
            when (node.namespace()) {
                "" -> Name.of(node.localName())
                else -> Name.of(node.namespace(), node.localName())
            }
        }

    actual var value: String?
        get() = node.attributeValue
        set(value) {
            node.setValue(value)
        }

    actual constructor(name: Name) : this(Attribute((name.namespace?.let { "$it:" }
        ?: "") + name.localName, null))

    actual constructor(name: Name, value: String?) : this(name) {
        this.value = value
    }
}

actual class RealDocumentFragment actual constructor(actual override val node: UnderlyingDocumentFragment) :
    RealElementLike() {
    override val element: Element
        get() = node

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated(
        message = "Attributes not supported on document objects",
        level = DeprecationLevel.ERROR
    )
    actual override val attributeSet: MutableSet<RealAttribute>
        get() = RealDocument.UnsupportedAttributeSet

    actual constructor() : this(Element("#fragment", Parser.NamespaceHtml))

    actual fun deepCopy(): RealDocumentFragment {
        return RealDocumentFragment(node.clone())
    }
}

actual data object RealUnknown : RealNode {
    actual override val node: UnderlyingBase
        get() = Comment("#unknown")
}
