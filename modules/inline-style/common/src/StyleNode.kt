package net.derfruhling.serenity.style

import net.derfruhling.serenity.tree.Apply

sealed class StyleNode : Apply<StyleNode, StyleNodeWithChildren> {
    override var parent: StyleNodeWithChildren? = null
        internal set

    override fun add(child: StyleNode) {
        throw UnsupportedOperationException("This node cannot have children")
    }

    override fun insert(index: Int, child: StyleNode) {
        throw UnsupportedOperationException("This node cannot have children")
    }

    override fun move(fromIndex: Int, toIndex: Int, count: Int) {
        throw UnsupportedOperationException("This node cannot have children")
    }

    override fun take(
        fromIndex: Int,
        count: Int
    ): List<StyleNode> {
        throw UnsupportedOperationException("This node cannot have children")
    }

    override fun remove(fromIndex: Int, count: Int) {
        throw UnsupportedOperationException("This node cannot have children")
    }

    override fun clear() {
        throw UnsupportedOperationException("This node cannot have children")
    }
}

