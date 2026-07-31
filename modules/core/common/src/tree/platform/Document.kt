package net.derfruhling.serenity.tree.platform

import net.derfruhling.serenity.Formatter
import net.derfruhling.serenity.event.EventTarget

open class Document(node: RealDocument) : DocumentLike<Document, RealDocument>(node),
                                          RootNode,
                                          EventTarget {
    init {
        updateReal()
    }

    @set:Deprecated("Document cannot have a parent", level = DeprecationLevel.ERROR)
    override var parent: NodeWithChildren<*, *>?
        get() = null
        set(_) {
            throw UnsupportedOperationException("cannot set parent of document")
        }

    @get:Deprecated("Document cannot have a parent", level = DeprecationLevel.ERROR)
    override val index: Index<Nothing>
        get() = throw UnsupportedOperationException("document has no parent")

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("Document cannot have a parent", level = DeprecationLevel.ERROR)
    override fun testAttribute() {
        throw UnsupportedOperationException("cannot insert attributes into document root")
    }

    override fun existingFrom(child: RealNode): ChildNode<in Document>? {
        return when (child) {
            is RealDocumentType -> DocumentTypeNode(child)
            else -> simpleExisting(child)
        }
    }

    override fun format(fmt: Formatter) {
        fmt.enter(Formatter.Begin.DOCUMENT, "Document") {
            for (child in children) {
                child.format(fmt)
            }
        }
    }

    companion object
}

