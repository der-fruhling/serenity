package net.derfruhling.serenity.platform

import net.derfruhling.serenity.Formatter
import net.derfruhling.serenity.attribute.HtmlAttributes

class InlineStyleNode() : ChildNode<ElementNode> {
    override val index: Index<InlineStyleNode> = Index(this)
    override var parent: ElementNode? = null

    private lateinit var _style: StyleHolder

    var style: StyleHolder
        get() = _style
        set(value) {
            _style = value
            value.setNotifyChanged(this::notifyChanged)
        }

    constructor(from: InlineStyleNode) : this() {
        if (from::_style.isInitialized) {
            style = from._style.copy()
        }
    }

    override fun format(fmt: Formatter) {
        fmt.block("InlineStyle") {
            if (::_style.isInitialized) {
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
        parent?.attribute(HtmlAttributes.style, style.makeStyle())
    }

    override fun deepCopy() = InlineStyleNode(this)
}
