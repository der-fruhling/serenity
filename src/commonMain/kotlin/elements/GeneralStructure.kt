package net.derfruhling.html.elements

import androidx.compose.runtime.Composable
import net.derfruhling.html.attribute.Attributes
import net.derfruhling.html.attribute.StringSetComposableAttribute

abstract class GeneralStructure<T: GeneralStructure<T>> {
    abstract class Attr {
        val classes = StringSetComposableAttribute(Attributes.`class`)
    }

    inline fun with(fn: T.() -> Unit) {
        @Suppress("UNCHECKED_CAST")
        (this as T).fn()
    }
}
