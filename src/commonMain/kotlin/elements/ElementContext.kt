package net.derfruhling.serenity.elements

import androidx.compose.runtime.Composable

interface ElementContext<A> {
    val attributes: A
}

inline fun <A, E: ElementContext<A>> E.attributes(fn: A.() -> Unit) {
    attributes.fn()
}

interface AttributeContext
