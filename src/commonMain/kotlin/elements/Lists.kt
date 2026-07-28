package net.derfruhling.serenity.elements

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.Text

abstract class GenericList<T: GenericList<T>> : GeneralStructure<T>() {
    abstract class Attr : GeneralStructure.Attr() {

    }

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

object OrderedList : GenericList<OrderedList>(), ElementContext<OrderedList.Attr> {
    object Attr : GenericList.Attr() {}

    override val attributes: Attr
        get() = Attr
}

@Composable
fun OrderedList(fn: @Composable OrderedList.() -> Unit) {
    Element("ol") {
        OrderedList.with { fn() }
    }
}

object UnorderedList : GenericList<UnorderedList>(), ElementContext<UnorderedList.Attr> {
    object Attr : GenericList.Attr() {}

    override val attributes: Attr
        get() = Attr
}

@Composable
fun UnorderedList(fn: @Composable UnorderedList.() -> Unit) {
    Element("ul") {
        UnorderedList.with { fn() }
    }
}
