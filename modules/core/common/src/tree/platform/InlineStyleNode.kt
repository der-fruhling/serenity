package net.derfruhling.serenity.tree.platform

import net.derfruhling.serenity.Formatter
import net.derfruhling.serenity.attribute.Attributes

class InlineStyleNode : ChildNode<ElementNode> {
    override val index: Index<InlineStyleNode> = Index(this)
    override var parent: ElementNode? = null

    lateinit var style: StyleHolder

    override fun format(fmt: Formatter) {
        TODO("Not yet implemented")
    }

    override fun reparent(newParent: NodeWithChildren<*, *>) {
        require(newParent is ElementNode) { "Inline styles may only be applied to elements" }
        parent = newParent
    }

    fun notifyChanged() {
        parent?.attribute(Attributes.style, style.makeStyle())
    }
}
