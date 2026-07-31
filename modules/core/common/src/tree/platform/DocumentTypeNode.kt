package net.derfruhling.serenity.tree.platform

import net.derfruhling.serenity.Formatter

class DocumentTypeNode : ComposeNodeWithReal<RealDocumentType>, ChildNode<DocumentLike<*, *>> {
    override val index: Index<DocumentTypeNode> = Index(this)
    override var parent: DocumentLike<*, *>? = null

    @Target(AnnotationTarget.PROPERTY_SETTER)
    @Retention(AnnotationRetention.BINARY)
    @RequiresOptIn(
        "Document type nodes cannot be updated after they're applied, ensure you're not " +
            "trying to do so. Modification after realization will fail with IllegalStateException."
    )
    @MustBeDocumented
    annotation class RequiresRealizationCheck

    val isRealized by ::isRealInitialized

    var type = ""
        @RequiresRealizationCheck
        set(value) {
            if (isRealized) error("Cannot update document type after realization")
            field = value
        }

    var public = ""
        @RequiresRealizationCheck
        set(value) {
            if (isRealized) error("Cannot update document type after realization")
            field = value
        }

    var system = ""
        @RequiresRealizationCheck
        set(value) {
            if (isRealized) error("Cannot update document type after realization")
            field = value
        }

    override fun realize() {
        if (!isRealized) {
            real = RealDocumentType(type, public, system)
        }
    }

    constructor() : super()

    constructor(real: RealDocumentType) : super(real)

    override fun format(fmt: Formatter) {
        fmt.enter(Formatter.Begin.VALUE) {
            if (isRealized) {
                write(
                    "<!DOCTYPE ${
                        arrayOf(
                            real.type.takeIf { it.isNotEmpty() },
                            real.public.takeIf { it.isNotEmpty() }?.let { "PUBLIC \"$it\"" },
                            real.system.takeIf { it.isNotEmpty() }?.let { "SYSTEM \"$it\"" },
                        ).filterNotNull().joinToString(" ")
                    }>"
                )
            } else {
                write("(doctype not yet realized)")
            }
        }
    }

    override fun reparent(newParent: NodeWithChildren<*, *>) {
        require(newParent is Document) { "Document types can only be added to documents" }
        parent = newParent
    }
}