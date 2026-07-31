package net.derfruhling.serenity.tree.platform

import androidx.compose.runtime.Applier
import net.derfruhling.serenity.Stack
import net.derfruhling.serenity.tree.HtmlApplier
import web.dom.document

actual sealed interface RootNode

actual open class PlatformApplier actual constructor(document: RootNode) :
    Applier<ComposeNode>,
    HtmlApplier {
    private val stack = Stack<ComposeNode>()

    actual final override var current: ComposeNode = when (document) {
        is Document -> document
        is DocumentFragment -> document
    }
        private set
    actual override var reflowTransformer: ((String) -> String)? = null // TODO

    actual override fun down(node: ComposeNode) {
        stack.push(current)
        current = node
    }

    actual override fun up() {
        current = stack.pop()
    }

    actual override fun insertTopDown(index: Int, instance: ComposeNode) {}

    private inline fun <R> withChildren(f: (NodeWithChildren<*, *>) -> R): R {
        val value = current
        require(value is NodeWithChildren<*, *>) { "The current node cannot have children" }
        return f(value)
    }

    actual override fun insertBottomUp(index: Int, instance: ComposeNode) {
        require(instance is ChildNode<*>)

        withChildren { it.insert(index, instance) }
    }

    actual override fun remove(index: Int, count: Int) {
        withChildren { it.remove(index, count) }
    }

    actual override fun move(from: Int, to: Int, count: Int) {
        withChildren { it.move(from, to, count) }
    }

    actual override fun clear() {
        if (!document.hidden) {
            withChildren { it.clear() }
        }
    }

    actual override fun reuse() {
        current.reuse()
    }
}