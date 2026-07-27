package net.derfruhling.html.tree.platform

import net.derfruhling.html.Formatter

open class Document(node: RealDocument) : NodeWithChildren<RealDocument>(node), RootNode {
    init {
        updateReal()
    }

    @set:Deprecated("Document cannot have a parent", level = DeprecationLevel.ERROR)
    override var parent: NodeWithChildren<RealDocument>?
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

    override fun format(fmt: Formatter) {
        fmt.enter(Formatter.Begin.DOCUMENT, "Document") {
            for(child in children) {
                child.format(fmt)
            }
        }
    }

    companion object
}

