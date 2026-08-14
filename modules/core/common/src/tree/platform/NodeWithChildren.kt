package net.derfruhling.serenity.tree.platform

import net.derfruhling.serenity.Name
import net.derfruhling.serenity.attribute.AttributeValue
import net.derfruhling.serenity.attribute.UntypedAttribute
import net.derfruhling.serenity.tree.Apply

sealed class NodeWithChildren<This : NodeWithChildren<This, U>, U : RealElementLike> :
    ComposeNodeWithReal<U>,
    Apply<ChildNode<*>, NodeWithChildren<*, *>> {
    override fun updateReal() {
        attributeIndices.clear()
        childIndices.clear()
        children.clear()

        for (attr in real.attributeSet) {
            addExisting(AttributeNode<Any>(attr))
        }

        for (child in real.children) {
            addExisting(existingFrom(child) ?: continue)
        }
    }

    internal fun simpleExisting(child: RealNode): ChildNode<NodeWithChildren<*, *>>? {
        return when (child) {
            is RealComment -> CommentNode(child)
            is RealElement -> ElementRegistry.derive(child)
            RealUnknown -> null
            else -> error("invalid node: $child")
        }
    }

    internal abstract fun existingFrom(child: RealNode): ChildNode<in This>?

    constructor() : super()
    constructor(from: U) : super(from)

    protected val attributeIndices = mutableListOf<UntypedAttribute>()
    protected val childIndices = mutableListOf<Index<*>>()
    val children: List<ChildNode<*>>
        field = mutableListOf()

    internal open fun testAttribute() {}

    override fun add(child: ChildNode<*>) {
        child.realize()
        child.index.index = children.size
        children.add(child)
        when (child) {
            is AttributeNode<*> -> {
                testAttribute()
                real.attributeSet.add(
                    RealAttribute(
                        child.name,
                        child.value?.let { AttributeValue.of(it) })
                )
                attributeIndices.add(child.parser)
            }

            is ComposeNodeWithReal<*> -> {
                real.children.add(child.real)
                childIndices.add(child.index)
            }

            else -> {}
        }

        child.reparent(this)
        child.applied()
    }

    private fun addExisting(child: ChildNode<*>) {
        child.index.index = children.size
        children.add(child)
        when (child) {
            is AttributeNode<*> -> {
                testAttribute()
                attributeIndices.add(child.parser)
            }

            is ComposeNodeWithReal<*> -> {
                childIndices.add(child.index)
            }

            else -> {}
        }

        child.reparent(this)
        child.applied()
    }

    override fun insert(index: Int, child: ChildNode<*>) {
        child.realize()
        child.index.index = index
        children.add(index, child)
        ((index + 1)..<children.size).forEach { i -> children[i].index.index = i }

        when (child) {
            is AttributeNode<*> -> {
                testAttribute()
                val insertAfterChild =
                    ((index - 1) downTo 0).firstOrNull { children[it] is AttributeNode<*> } ?: -1
                val newIndex =
                    if (insertAfterChild >= 0) attributeIndices.indexOf((children[insertAfterChild] as AttributeNode<*>).parser) + 1 else 0
                real.attributeSet.add(
                    RealAttribute(
                        child.name,
                        child.value?.let { AttributeValue.of(it) })
                )
                attributeIndices.add(newIndex, child.parser)
            }

            is ComposeNodeWithReal<*> -> {
                val insertAfterChild =
                    ((index - 1) downTo 0).firstOrNull { children[it] !is AttributeNode<*> } ?: -1
                val newIndex =
                    if (insertAfterChild >= 0) childIndices.indexOf(children[insertAfterChild].index) + 1 else 0
                real.children.add(newIndex, child.real)
                childIndices.add(newIndex, child.index)
            }

            else -> {}
        }

        child.reparent(this)
        child.applied()
    }

    override fun move(fromIndex: Int, toIndex: Int, count: Int) {
        val values = take(fromIndex, count)

        for ((i, child) in ((toIndex - count)..<toIndex).zip(values)) {
            insert(i, child)
        }
    }

    override fun take(
        fromIndex: Int,
        count: Int
    ): List<ChildNode<*>> {
        return List(count) {
            children.removeAt(fromIndex).also { it.removed() }
        }.also { list ->
            list.forEach {
                it.index.index = -1

                when (it) {
                    is AttributeNode<*> -> {
                        val index = attributeIndices.indexOf(it.parser)
                        real.attributeSet.remove(it.real)
                        attributeIndices.removeAt(index)
                    }

                    else -> {
                        val index = childIndices.indexOf(it.index)
                        real.children.removeAt(index)
                        childIndices.removeAt(index)
                    }
                }
            }

            if (children.size >= fromIndex) {
                for (i in fromIndex..<children.size) {
                    children[i].index.index -= count
                }
            }
        }
    }

    override fun remove(fromIndex: Int, count: Int) {
        take(fromIndex, count)
    }

    override fun clear() {
        take(0, children.size)
    }

    val descendents: Iterable<ChildNode<*>> = Iterable {
        iterator {
            for (node in children) {
                yield(node)

                if (node is NodeWithChildren<*, *>) {
                    yieldAll(node.descendents)
                }
            }
        }
    }

    fun findImmediateElementNamed(name: Name): ElementNode? {
        return children.find { it is ElementNode && it.name == name } as ElementNode?
    }

    fun findImmediateElementIndexNamed(name: Name): Int {
        return children.indexOfFirst { it is ElementNode && it.name == name }
    }

    inline fun findImmediateElementNamed(
        name: Name,
        filter: (ElementNode) -> Boolean
    ): ElementNode? {
        return children.find { it is ElementNode && it.name == name && filter(it) } as ElementNode?
    }

    fun descendentsMaxDepth(maxDepth: Int): Iterable<ChildNode<*>> {
        return Iterable {
            iterator {
                for (node in children) {
                    yield(node)
                }

                if (maxDepth > 0) {
                    for (node in children) {
                        if (node is NodeWithChildren<*, *>) {
                            yieldAll(node.descendentsMaxDepth(maxDepth - 1))
                        }
                    }
                }
            }
        }
    }

    fun findDescendentNamed(name: Name): ElementNode? {
        return descendents.find { it is ElementNode && it.name == name } as ElementNode?
    }

    fun findDescendentNamed(name: Name, maxDepth: Int): ElementNode? {
        return descendentsMaxDepth(maxDepth).find { it is ElementNode && it.name == name } as ElementNode?
    }

    override fun reuse() {
        /*attributeIndices.clear()
        childIndices.clear()
        children.clear()

        real.attributeSet.clear()
        real.children.clear()*/
    }
}