package net.derfruhling.html.tree

interface Apply<T, out P> {
    val parent: P?

    fun add(child: T)
    fun insert(index: Int, child: T)
    fun move(fromIndex: Int, toIndex: Int, count: Int)
    fun take(fromIndex: Int, count: Int): List<T>
    fun remove(fromIndex: Int, count: Int)
    fun clear()
}