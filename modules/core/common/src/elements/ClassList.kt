package net.derfruhling.serenity.elements

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.annotation.RememberInComposition
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.snapshots.SnapshotStateSet

@Stable
sealed class ClassList(private val classes: Set<String>) : Set<String> {
    override fun contains(element: String): Boolean {
        return element in classes
    }

    override fun containsAll(elements: Collection<String>): Boolean {
        return classes.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return classes.isEmpty()
    }

    override fun iterator(): Iterator<String> {
        return classes.iterator()
    }

    override val size: Int
        get() = classes.size

    companion object {
        val EMPTY: EmptyClassList = EmptyClassList
    }

    override fun toString(): String {
        return classes.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClassList) return false

        if (classes != other.classes) return false

        return true
    }

    override fun hashCode(): Int {
        return classes.hashCode()
    }
}

@Immutable
data object EmptyClassList : ClassList(emptySet())

@Immutable
private class ImmutableClassListImpl(classes: Set<String>) : ClassList(classes)

@Stable
class MutableClassList internal constructor(val asStateSet: SnapshotStateSet<String>) : ClassList(
    asStateSet
), MutableSet<String> by asStateSet {
    override fun iterator(): MutableIterator<String> {
        return asStateSet.iterator()
    }

    override fun contains(element: String): Boolean {
        return element in asStateSet
    }

    override fun containsAll(elements: Collection<String>): Boolean {
        return asStateSet.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return asStateSet.isEmpty()
    }

    override val size: Int
        get() = asStateSet.size
}

@RememberInComposition
fun mutableClassListOf() = MutableClassList(SnapshotStateSet())

@RememberInComposition
fun mutableClassListOf(vararg className: String) = MutableClassList(mutableStateSetOf(*className))

@RememberInComposition
fun mutableClassListOf(stateSet: SnapshotStateSet<String>) = MutableClassList(stateSet)

@RememberInComposition
fun mutableClassListOf(set: Set<String>) =
    MutableClassList(SnapshotStateSet<String>().also { it += set })

fun classes(set: Iterable<String>): ClassList = ImmutableClassListImpl(set.toSet())
fun classes(className: String): ClassList = ImmutableClassListImpl(setOf(className))
fun classes(vararg classes: String): ClassList = ImmutableClassListImpl(setOf(*classes))
inline fun classes(fn: MutableSet<String>.() -> Unit) = classes(buildSet(fn))
