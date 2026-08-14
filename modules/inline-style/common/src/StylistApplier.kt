package net.derfruhling.serenity.style

import androidx.compose.runtime.Applier
import net.derfruhling.serenity.Stack

class StylistApplier<T: StyleNode>(val target: StylistTarget<T>) : Applier<T> {
    private val stack: Stack<T> = Stack()
    private var _current: T? = null
    override val current: T get() = _current!!

    override fun onBeginChanges() {
        target.start()
    }

    override fun onEndChanges() {
        target.end()
    }

    override fun clear() {
        (_current ?: target).clear()
    }

    override fun down(node: T) {
        _current?.let { stack.push(it) }
        _current = node
    }

    override fun insertBottomUp(
        index: Int,
        instance: T
    ) {
        when(val c = _current) {
            null -> target.add(instance)
            else -> c.add(instance)
        }
    }

    override fun insertTopDown(
        index: Int,
        instance: T
    ) {}

    override fun move(from: Int, to: Int, count: Int) {
        (_current ?: target).move(from, to, count)
    }

    override fun remove(index: Int, count: Int) {
        (_current ?: target).remove(index, count)
    }

    override fun up() {
        _current = stack.pop()
    }
}