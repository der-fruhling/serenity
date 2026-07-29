package net.derfruhling.serenity.tree.platform

import net.derfruhling.serenity.Formatter

class CommentNode(real: RealComment) : ComposeNodeWithReal<RealComment>(real), ChildNode<NodeWithChildren<*>> {
    constructor() : this(RealComment())

    override val index: Index<CommentNode> = Index(this)
    override var parent: NodeWithChildren<*>? = null

    override fun reparent(newParent: NodeWithChildren<*>) {
        parent = newParent
    }

    var commentContent: String by real::commentContent

    override fun format(fmt: Formatter) {
        if(fmt.deepIntrospect) {
            fmt.enter(Formatter.Begin.DEBUG, "Comment") {
                write(commentContent)
            }
        }
    }
}
