package net.derfruhling.serenity.style.notations

import net.derfruhling.serenity.style.Notation
import net.derfruhling.serenity.style.UnparsableNotationException
import kotlin.enums.EnumEntries
import kotlin.enums.enumEntries
import kotlin.reflect.KClass

interface NotableEnum {
    val notationString: String
}

private val hyphenate = Regex("([a-z])([A-Z])")

private val Enum<*>.autoNotate: String
    get() = (this as? NotableEnum)?.notationString ?: hyphenate.replace(name) {
        val (lower, upper) = it.destructured
        "$lower-$upper"
    }.lowercase().replace('_', '-')

class EnumNotation<T : Enum<T>>(val kClass: KClass<T>, entries: EnumEntries<T>) : Notation<T> {
    val entryMap = entries.associateBy { it.autoNotate }
    val nameMap by lazy { entryMap.entries.associate { (n, e) -> e to n } }

    override fun test(value: Any): Boolean {
        return kClass.isInstance(value)
    }

    override fun asNotationString(value: T): String {
        return nameMap[value]!!
    }

    override fun fromNotationString(value: String): T {
        return entryMap[value]
            ?: throw UnparsableNotationException("Unknown enumeration value '$value'")
    }
}

inline fun <reified T : Enum<T>> enumNotation(): Lazy<EnumNotation<T>> {
    return lazy {
        EnumNotation(T::class, enumEntries<T>())
    }
}
