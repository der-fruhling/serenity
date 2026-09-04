package net.derfruhling.serenity.platform

fun interface Matcher<in T> {
    fun match(element: T): Boolean
}