package net.derfruhling.serenity.tree.platform

fun interface Matcher<in T> {
    fun match(element: T): Boolean
}