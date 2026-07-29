package net.derfruhling.serenity.tree.platform

import net.derfruhling.serenity.Formatter

class TextNode(real: RealText) : ComposeNodeWithReal<RealText>(real), ChildNode<ElementNode> {
    constructor() : this(RealText())

    constructor(textContent: String) : this() {
        this.textContent = textContent
    }

    override val index: Index<TextNode> = Index(this)
    override var parent: ElementNode? = null

    override fun reparent(newParent: NodeWithChildren<*>) {
        require(newParent is ElementNode) { "Text cannot be applied to non-elements" }
        parent = newParent
    }

    var textContent: String by real::textContent

    override fun format(fmt: Formatter) {
        fmt.enter(Formatter.Begin.TEXT, "Text") {
            write(textContent)
        }
    }
}
