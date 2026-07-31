package net.derfruhling.serenity.elements

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.Text

abstract class GenericList<T : GenericList<T>> {
    @Composable
    fun Entry(fn: @Composable () -> Unit) {
        Element("li", fn)
    }

    @Composable
    fun Entry(text: String) {
        Element("li") {
            Text(text)
        }
    }

    @Composable
    fun Entry(text: String, fn: @Composable () -> Unit) {
        Element("li") {
            Text(text)
            fn()
        }
    }
}

object OrderedList : GenericList<OrderedList>()

@Composable
fun OrderedList(fn: @Composable OrderedList.() -> Unit) {
    Element("ol") {
        OrderedList.apply { fn() }
    }
}

object UnorderedList : GenericList<UnorderedList>()

@Composable
fun UnorderedList(fn: @Composable UnorderedList.() -> Unit) {
    Element("ul") {
        UnorderedList.apply { fn() }
    }
}
