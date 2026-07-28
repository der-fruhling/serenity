package net.derfruhling.serenity.tree.platform

import net.derfruhling.serenity.Formatter

open class DocumentFragment(node: RealDocumentFragment) : NodeWithChildren<RealDocumentFragment>(node), RootNode {
    @set:Deprecated("Document cannot have a parent", level = DeprecationLevel.ERROR)
    override var parent: NodeWithChildren<RealDocumentFragment>?
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
        fmt.enter(Formatter.Begin.DOCUMENT, "DocumentFragment") {
            for(child in children) {
                child.format(fmt)
            }
        }
    }

    constructor() : this(RealDocumentFragment())
}
