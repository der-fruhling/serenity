package net.derfruhling.html.tree.platform

sealed interface ChildNode<T : NodeWithChildren<*>> : ComposeNode {
    var parent: T?

    fun reparent(newParent: NodeWithChildren<*>)
}
