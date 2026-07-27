package net.derfruhling.html

class Stack<T> {
    private val list = ArrayList<T>()

    fun push(value: T) {
        list.add(value)
    }

    fun pop(): T = list.removeLast()
}