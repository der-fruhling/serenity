package net.derfruhling.serenity

class Stack<T> {
    private val list = ArrayList<T>()

    val isEmpty: Boolean
        get() = list.isEmpty()

    fun push(value: T) {
        list.add(value)
    }

    fun peek(): T = list.last()

    fun pop(): T = list.removeLast()

    fun clear() = list.clear()
}