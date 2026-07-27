package net.derfruhling.html.attribute

import net.derfruhling.html.Name
import kotlin.reflect.KClass

class AttributeBuilder<T : Any>(val name: Name, val kClass: KClass<T>) {
    var permitExplicitSet: Boolean = true
    var defaultValue: (() -> T)? = null
    lateinit var parser: (String?) -> T?

    init {
        @Suppress("UNCHECKED_CAST")
        when (kClass) {
            String::class -> {
                parser = { it as T? }
            }

            Boolean::class -> {
                parser = { (it != null) as T }
            }
        }
    }

    fun defaultValue(fn: () -> T) {
        defaultValue = fn
    }

    fun parser(fn: (String?) -> T?) {
        parser = fn
    }

    fun build() = ConfiguredAttribute(
        name,
        parser,
        kClass,
        permitExplicitSet,
        defaultValue
    )
}