package net.derfruhling.html.test

import kotlinx.coroutines.Dispatchers
import net.derfruhling.html.tree.HtmlCompositionContext

fun useContext(fn: context(HtmlCompositionContext) () -> Unit) {
    HtmlCompositionContext(Dispatchers.Default).use {
        context(it) {
            fn()
        }
    }
}