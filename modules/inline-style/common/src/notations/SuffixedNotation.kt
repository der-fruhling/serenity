package net.derfruhling.serenity.style.notations

import net.derfruhling.serenity.style.Notation
import net.derfruhling.serenity.style.optRequire

abstract class SuffixedNotation<T, U>(val base: Notation<T>, val suffix: String) : Notation<U> {
    abstract fun create(base: T): U
    abstract val U.asBase: T

    override fun asNotationString(value: U): String {
        return base.asNotationString(value.asBase) + suffix
    }

    override fun fromNotationString(value: String): U {
        optRequire(value.endsWith(suffix)) { "Value '$value' does not end with '$suffix'" }
        return create(base.fromNotationString(value.substring(0, value.length - suffix.length)))
    }
}