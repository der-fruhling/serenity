package net.derfruhling.serenity.tree.platform

sealed interface ChildNode<T : NodeWithChildren<*, *>> : ComposeNode {
    var parent: T?

    fun reparent(newParent: NodeWithChildren<*, *>)
}
