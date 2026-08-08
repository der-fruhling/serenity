package net.derfruhling.serenity.tree.platform

import net.derfruhling.serenity.Formatter
import net.derfruhling.serenity.annotations.UnescapedTextDanger

class DataNode(real: RealData) : ComposeNodeWithReal<RealData>(real), ChildNode<ElementNode> {
    constructor() : this(RealData())

    @UnescapedTextDanger
    constructor(textContent: String) : this() {
        this.textContent = textContent
    }

    override val index: Index<DataNode> = Index(this)
    override var parent: ElementNode? = null

    override fun reparent(newParent: NodeWithChildren<*, *>) {
        require(newParent is ElementNode) { "Text cannot be applied to non-elements" }
        parent = newParent
    }

    @set:UnescapedTextDanger
    var textContent: String by real::textContent

    override fun format(fmt: Formatter) {
        fmt.enter(Formatter.Begin.TEXT, "Text") {
            write(textContent)
        }
    }

    @OptIn(UnescapedTextDanger::class)
    override fun reuse() {
        textContent = ""
    }
}
