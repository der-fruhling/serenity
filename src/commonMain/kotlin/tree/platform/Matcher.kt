package net.derfruhling.html.tree.platform

fun interface Matcher<in T> {
    fun match(element: T): Boolean
}