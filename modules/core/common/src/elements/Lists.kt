package net.derfruhling.serenity.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.snapshots.SnapshotStateSet
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.Text
import net.derfruhling.serenity.attribute.HtmlAttributes

@Suppress("NOTHING_TO_INLINE")
@Composable
private inline fun <T> eachImpl(set: Set<T>, noinline fn: @Composable (T) -> Unit) {
    val map = remember { mutableMapOf<T, @Composable (T) -> Unit>() }

    for (item in set) {
        val movableContent = map.getOrPut(item) { movableContentOf(fn) }

        movableContent(item)
    }

    map.keys.retainAll(set)
}

@Composable
fun <T> Set<T>.forEachMovable(fn: @Composable (T) -> Unit) {
    eachImpl(this, fn)
}

@Composable
fun <T> SnapshotStateSet<T>.forEachMovable(fn: @Composable (T) -> Unit) {
    eachImpl(this, fn)
}

@Suppress("NOTHING_TO_INLINE")
@Composable
private inline fun <K, V> eachImpl(values: Map<K, V>, noinline fn: @Composable (K, V) -> Unit) {
    val map = remember { mutableMapOf<K, @Composable (K, V) -> Unit>() }

    for ((key, value) in values) {
        val movableContent = map.getOrPut(key) { movableContentOf(fn) }

        movableContent(key, value)
    }

    map.keys.retainAll(values.keys)
}

@Composable
fun <K, V> Map<K, V>.forEachMovable(fn: @Composable (K, V) -> Unit) {
    eachImpl(this, fn)
}

@Composable
fun <K, V> SnapshotStateMap<K, V>.forEachMovable(fn: @Composable (K, V) -> Unit) {
    eachImpl(this, fn)
}

abstract class GenericList<T : GenericList<T>> {
    @Composable
    fun Entry(text: String = "", fn: @Composable () -> Unit = {}) {
        Element("li") {
            Text(text)
            fn()
        }
    }
}

object OrderedList : GenericList<OrderedList>()

@Composable
fun OrderedList(
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable OrderedList.() -> Unit
) {
    Element("ol", update = {
        apply(classList)
        attribute(HtmlAttributes.id, id)
    }) {
        OrderedList.apply { fn() }
    }
}

@Composable
fun <T> OrderedList(
    iterable: Iterable<T>,
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable (T) -> Unit
) {
    OrderedList(classList, id) {
        for (item in iterable) {
            Entry {
                fn(item)
            }
        }
    }
}

@Composable
fun <T> OrderedList(
    list: SnapshotStateList<T>,
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable (T) -> Unit
) {
    OrderedList(list as Iterable<T>, classList, id, fn)
}

object UnorderedList : GenericList<UnorderedList>()

@Composable
fun UnorderedList(
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable UnorderedList.() -> Unit
) {
    Element("ul", update = {
        apply(classList)
        attribute(HtmlAttributes.id, id)
    }) {
        UnorderedList.apply { fn() }
    }
}

@Composable
fun <T> UnorderedList(
    set: Set<T>,
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable (T) -> Unit
) {
    UnorderedList(classList, id) {
        set.forEachMovable {
            Entry { fn(it) }
        }
    }
}

@Composable
fun <T> UnorderedList(
    set: SnapshotStateSet<T>,
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable (T) -> Unit
) {
    UnorderedList(set as Set<T>, classList, id, fn)
}

@Composable
fun <T> UnorderedList(
    list: Iterable<T>,
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable (T) -> Unit
) {
    UnorderedList(classList, id) {
        for (item in list) {
            Entry {
                fn(item)
            }
        }
    }
}

@Composable
fun <T> UnorderedList(
    list: SnapshotStateList<T>,
    classList: ClassList = ClassList.EMPTY,
    id: String? = null,
    fn: @Composable (T) -> Unit
) {
    UnorderedList(list as Iterable<T>, classList, id, fn)
}
