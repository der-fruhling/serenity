package net.derfruhling.serenity.style

import net.derfruhling.serenity.tree.Apply
import net.derfruhling.serenity.tree.platform.StyleHolder

class InlineStyle : StylistTarget<RuleNode>, StyleHolder {
    val rules: List<RuleNode>
        field = mutableListOf()

    private var _notifyChanged: (() -> Unit)? = null

    override fun removed() {
        _notifyChanged = null
    }

    override fun setNotifyChanged(fn: () -> Unit) {
        _notifyChanged = fn
        fn()
    }

    internal fun notifyChanged() {
        _notifyChanged?.invoke()
    }

    private fun removeParent(child: RuleNode): RuleNode =
        child.also {
            it.parent = null
        }

    private fun replaceParent(child: RuleNode): RuleNode =
        child.also {
            it.parent?.remove(it)
            it.parent = null
        }

    override fun add(child: RuleNode) {
        rules.add(replaceParent(child))
    }

    override fun insert(index: Int, child: RuleNode) {
        rules.add(index, replaceParent(child))
    }

    override fun move(fromIndex: Int, toIndex: Int, count: Int) {
        rules.addAll(toIndex - count, take(fromIndex, count).map { replaceParent(it) })
    }

    override fun take(
        fromIndex: Int,
        count: Int
    ): List<RuleNode> {
        return buildList {
            val i = rules.listIterator(fromIndex)

            repeat(count) {
                add(removeParent(i.next()))
                i.remove()
            }
        }
    }

    fun remove(childNode: RuleNode) {
        rules.remove(childNode)
        removeParent(childNode)
    }

    override fun remove(fromIndex: Int, count: Int) {
        val i = rules.listIterator(fromIndex)

        repeat(count) {
            removeParent(i.next())
            i.remove()
        }
    }

    override fun clear() {
        for(c in rules) removeParent(c)
        rules.clear()
    }

    override fun start() {}

    override fun end() {
        notifyChanged()
    }

    override fun makeStyle(): String {
        return rules.joinToString("; ") { "${it.ruleName}: ${it.ruleValue}" }
    }
}
