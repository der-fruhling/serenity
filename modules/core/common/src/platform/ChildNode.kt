package net.derfruhling.serenity.platform

sealed interface ChildNode<T : NodeWithChildren<*, *>> : ComposeNode {
    var parent: T?

    fun reparent(newParent: NodeWithChildren<*, *>)

    abstract override fun deepCopy(): ChildNode<T>
}
