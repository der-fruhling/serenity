@file:Suppress("DEPRECATION")

package net.derfruhling.serenity.tree.platform

import js.array.asList
import net.derfruhling.serenity.Name
import net.derfruhling.serenity.catch
import net.derfruhling.serenity.event.EventSubscriptionHandle
import net.derfruhling.serenity.event.EventType
import web.dom.Node
import web.dom.ParentNode
import web.dom.document
import web.events.*
import web.events.EventType as WebEventType

private const val HTML_NS = "http://www.w3.org/1999/xhtml"

actual fun RealNode(base: UnderlyingBase): RealNode? {
    // No, kotlin. This when is not exhaustive
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    return when (base.nodeType) {
        Node.DOCUMENT_NODE -> RealDocument(base as UnderlyingDocument)
        Node.ELEMENT_NODE -> RealElement(base as UnderlyingElement)
        Node.TEXT_NODE -> RealText(base as UnderlyingText)
        Node.COMMENT_NODE -> RealComment(base as UnderlyingComment)
        Node.ATTRIBUTE_NODE -> RealAttribute(base as UnderlyingAttribute)
        Node.DOCUMENT_TYPE_NODE -> RealDocumentType(base as UnderlyingDocType)
        else -> null
    }
}

actual class RealText actual constructor(actual override val node: UnderlyingText) : RealNode {
    actual var textContent: String
        get() = node.data
        set(value) {
            node.data = value
        }

    actual constructor() : this(document.createTextNode(""))
}

actual typealias RealData = RealText

actual class RealComment actual constructor(actual override val node: UnderlyingComment) : RealNode {
    actual var commentContent: String
        get() = node.data
        set(value) {
            node.data = value
        }

    actual constructor() : this(document.createComment(""))
}

actual class RealDocumentType actual constructor(actual override val node: UnderlyingDocType) :
    RealNode {
    actual val type: String = node.name
    actual val public: String = node.publicId
    actual val system: String = node.systemId

    actual constructor(from: RealDocumentType)
        : this(from.type, from.public, from.system)

    actual constructor(type: String, public: String, system: String)
        : this(document.implementation.createDocumentType(type, public, system))
}

actual sealed class RealElementLike : RealNode {
    internal abstract val parent: ParentNode

    actual abstract val attributeSet: MutableSet<RealAttribute>

    private inner class ChildList : AbstractMutableList<RealNode>() {
        override fun add(index: Int, element: RealNode) {
            when (index) {
                0 -> parent.prepend(element.node)
                size -> parent.append(element.node)
                else -> parent.childNodes[index].before(element.node)
            }
        }

        override fun removeAt(index: Int): RealNode {
            return RealNode(parent.childNodes[index].also { it.remove() }) ?: RealUnknown
        }

        override fun set(
            index: Int,
            element: RealNode
        ): RealNode {
            return RealNode(parent.childNodes[index].also {
                it.replaceWith(element.node)
            }) ?: RealUnknown
        }

        override fun get(index: Int): RealNode {
            return RealNode(parent.childNodes[index]) ?: RealUnknown
        }

        override val size: Int
            get() = parent.childNodes.length
    }

    actual open val children: MutableList<RealNode> by lazy { ChildList() }
}

actual class RealElement actual constructor(node: UnderlyingElement) : RealElementLike() {
    actual constructor(name: Name) : this(
        document.createElementNS(
            name.namespaceUrl ?: HTML_NS,
            (name.namespace?.let { "$it:" } ?: "") + name.localName
        )
    )

    actual override var node = node
        internal set

    override val parent: ParentNode
        get() = node

    actual val name: Name by lazy {
        val localName = node.localName
        val prefix = node.prefix
        if (prefix != null) {
            when (val nsUri = node.namespaceURI) {
                null -> Name.qualified(prefix, localName)
                else -> Name.qualified(prefix, nsUri, localName)
            }
        } else {
            when (val nsUri = node.namespaceURI) {
                null -> Name.of(localName)
                else -> Name.of(nsUri, localName)
            }
        }
    }

    inner class AttributeSet : AbstractMutableSet<RealAttribute>() {
        private val map = node.attributes.asList().associate { node ->
            RealAttribute(node).let { it.name to it }
        }.toMutableMap()

        override fun add(element: RealAttribute): Boolean {
            map[element.name] = element
            if (element.name.namespaceUrl != null) {
                node.setAttributeNS(
                    element.name.namespace,
                    (element.name.namespace?.let { "$it:" } ?: "") + element.name.localName,
                    element.value ?: "")
            } else {
                node.setAttribute((element.name.namespace?.let { "$it:" }
                    ?: "") + element.name.localName,
                                  element.value ?: "")
            }
            return true
        }

        override fun iterator(): MutableIterator<RealAttribute> {
            return object : MutableIterator<RealAttribute> {
                private val iterator = map.iterator()
                private lateinit var current: RealAttribute

                override fun remove() {
                    iterator.remove()

                    catch { node.removeAttributeNode(current.node) }
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

    actual override val attributeSet: MutableSet<RealAttribute> by lazy { AttributeSet() }

    actual inline fun <T> subscribe(
        type: EventType<T>,
        crossinline handler: (T) -> Unit
    ): EventSubscriptionHandle {
        val closure: EventHandler<Event, EventTarget, EventTarget> = EventHandler { e: Event ->
            handler(type.generate(e))
        }

        val eventType = WebEventType<Event>(type.name)
        node.addEventListener(eventType, closure)

        return EventSubscriptionHandle {
            node.removeEventListener(eventType, closure)
        }
    }
}

actual class RealDocument actual constructor(actual override val node: UnderlyingDocument) :
    RealElementLike() {
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

    override val parent: ParentNode
        get() = node

    private val _children = super.children

    override val children: MutableList<RealNode> = object : AbstractMutableList<RealNode>() {
        override fun add(index: Int, element: RealNode) {
            if (element is RealElement) {
                val newIndex = _children.indexOfFirst { it is RealElement }
                if (newIndex >= 0) this[newIndex] = element
                else _children.add(index, element)
            } else if (element is RealDocumentType) {
                val newIndex = _children.indexOfFirst { it is RealDocumentType }
                if (newIndex >= 0) this[newIndex] = element
                else _children.add(index, element)
            } else _children.add(index, element)
        }

        override fun removeAt(index: Int): RealNode {
            return _children.removeAt(index)
        }

        override fun set(
            index: Int,
            element: RealNode
        ): RealNode {
            when (element) {
                is RealElement -> {
                    node.documentElement.replaceChildren(*element.children.mapNotNull { (it as? RealElement)?.node }
                        .toTypedArray())

                    for (attr in element.attributeSet) {
                        node.documentElement.attributes.setNamedItemNS(attr.node)
                    }

                    element.node = node.documentElement

                    return element
                }

                is RealDocumentType -> {
                    if (node.doctype != null) {
                        node.doctype?.replaceWith(element.node)
                    } else {
                        node.prepend(element.node)
                    }

                    return element
                }

                else -> {
                    return _children.set(index, element)
                }
            }
        }

        override fun get(index: Int): RealNode {
            return _children[index]
        }

        override val size: Int
            get() = _children.size
    }

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("Attributes not supported on document objects", level = DeprecationLevel.ERROR)
    actual override val attributeSet: MutableSet<RealAttribute>
        get() = UnsupportedAttributeSet

    actual fun applyFragment(realDocumentFragment: RealDocumentFragment) {
        node.appendChild(realDocumentFragment.node)
    }

    companion object {
        val CURRENT = RealDocument(document)
    }
}

@Deprecated("Avoid if possible")
actual class RealAttribute actual constructor(actual override val node: UnderlyingAttribute) :
    RealNode {
    actual constructor(name: Name) : this(
        document.createAttributeNS(
            name.namespaceUrl ?: HTML_NS,
            (name.namespace?.let { "$it:" } ?: "") + name.localName
        )
    )

    actual constructor(name: Name, value: String?) : this(name) {
        this.value = value
    }

    actual val name: Name by lazy {
        val tagName = node.name
        if (':' in tagName) {
            val (ns, name) = tagName.split(':')
            when (val nsUri = node.namespaceURI) {
                null -> Name.qualified(ns, name)
                else -> Name.qualified(ns, nsUri, name)
            }
        } else {
            when (val nsUri = node.namespaceURI) {
                null -> Name.of(tagName)
                else -> Name.of(nsUri, tagName)
            }
        }
    }

    actual var value: String?
        get() = node.value
        set(value) {
            node.value = value ?: ""
        }
}

actual class RealDocumentFragment actual constructor(actual override val node: UnderlyingDocumentFragment) :
    RealElementLike() {
    override val parent: ParentNode
        get() = node

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated(
        message = "Attributes not supported on document objects",
        level = DeprecationLevel.ERROR
    )
    actual override val attributeSet: MutableSet<RealAttribute>
        get() = RealDocument.UnsupportedAttributeSet

    actual constructor() : this(document.createDocumentFragment())

    actual fun deepCopy(): RealDocumentFragment {
        return RealDocumentFragment(node.cloneNode(subtree = true) as UnderlyingDocumentFragment)
    }
}

actual data object RealUnknown : RealNode {
    actual override val node: UnderlyingBase
        get() = document.createComment("#unknown")
}
