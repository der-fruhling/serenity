package net.derfruhling.serenity.attribute

import net.derfruhling.serenity.Name

object AttributeRegistry {
    private val all = mutableMapOf<Name, Lazy<UntypedAttribute>>()

    internal fun registerAttribute(name: Name, attribute: Lazy<UntypedAttribute>) {
        if (name in all) throw IllegalStateException("Attribute $name already registered elsewhere")
        all[name] = attribute
    }

    fun attributeForName(name: Name): UntypedAttribute {
        return all[name]?.value ?: ExistingAttribute(name)
    }
}