package net.derfruhling.serenity.tree.platform

import net.derfruhling.serenity.Formatter
import net.derfruhling.serenity.attribute.Attributes

class InlineStyleNode : ChildNode<ElementNode> {
    override val index: Index<InlineStyleNode> = Index(this)
    override var parent: ElementNode? = null

    private lateinit var _style: StyleHolder

    var style: StyleHolder
        get() = _style
        set(value) {
            _style = value
            value.setNotifyChanged(this::notifyChanged)
        }

    override fun format(fmt: Formatter) {
        fmt.block("InlineStyle") {
            if(::_style.isInitialized) {
                write(style.makeStyle())
            } else {
                write("<null>")
            }
        }
    }

    override fun reparent(newParent: NodeWithChildren<*, *>) {
        require(newParent is ElementNode) { "Inline styles may only be applied to elements" }
        parent = newParent
    }

    fun notifyChanged() {
        parent?.attribute(Attributes.style, style.makeStyle())
    }
}
