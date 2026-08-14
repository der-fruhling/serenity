package net.derfruhling.serenity.style

sealed class StyleNodeWithChildren : StyleNode() {
    val children: List<StyleNode>
        field = mutableListOf()

    protected open fun removeParent(child: StyleNode): StyleNode =
        child.also {
            it.parent = null
        }

    protected open fun replaceParent(child: StyleNode): StyleNode =
        child.also {
            it.parent?.remove(it)
            it.parent = this
        }

    override fun add(child: StyleNode) {
        children.add(replaceParent(child))
    }

    override fun insert(index: Int, child: StyleNode) {
        children.add(index, replaceParent(child))
    }

    override fun move(fromIndex: Int, toIndex: Int, count: Int) {
        children.addAll(toIndex - count, take(fromIndex, count).map { replaceParent(it) })
    }

    override fun take(
        fromIndex: Int,
        count: Int
    ): List<StyleNode> {
        return buildList {
            val i = children.listIterator(fromIndex)

            repeat(count) {
                add(removeParent(i.next()))
                i.remove()
            }
        }
    }

    open fun remove(childNode: StyleNode) {
        children.remove(childNode)
        removeParent(childNode)
    }

    override fun remove(fromIndex: Int, count: Int) {
        val i = children.listIterator(fromIndex)

        repeat(count) {
            removeParent(i.next())
            i.remove()
        }
    }

    override fun clear() {
        for(c in children) removeParent(c)
        children.clear()
    }
}