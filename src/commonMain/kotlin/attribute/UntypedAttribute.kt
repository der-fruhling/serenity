package net.derfruhling.html.attribute

import net.derfruhling.html.Name

abstract class UntypedAttribute {
    abstract val name: Name
    abstract val parser: (String) -> Any?
    open val permitExplicitSet: Boolean get() = true
    open val defaultValue: (() -> Any?)? get() = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UntypedAttribute) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}