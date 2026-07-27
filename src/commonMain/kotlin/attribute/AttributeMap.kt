package net.derfruhling.html.attribute

import kotlin.reflect.cast
import kotlin.reflect.safeCast

abstract class AttributeMap {
    protected abstract fun <T : Any> explicitSet(attribute: Attribute<T>, value: T?)

    abstract operator fun get(attribute: UntypedAttribute): Any?

    operator fun set(attribute: UntypedAttribute, value: Any?) {
        put(attribute, value)
    }

    abstract fun put(attribute: UntypedAttribute, value: Any?): Any?
    abstract fun remove(attribute: UntypedAttribute): Any?

    protected open fun <T : Any> AttributeMap.defaultValueOf(attribute: Attribute<T>): T? =
        when(val default = attribute.defaultValue) {
            null -> null
            else -> attribute.kClass.safeCast(default()).also {
                explicitSet(attribute, it)
            }
        }

    fun <T : Any> getValue(attribute: Attribute<T>): T? =
        this[attribute as UntypedAttribute]?.let { attribute.kClass.cast(it) }

    operator fun <T : Any> get(attribute: Attribute<T>): T? =
        getValue(attribute)
            ?: defaultValueOf(attribute)

    fun <T : Any> put(attribute: Attribute<T>, value: T?): T? {
        if(!attribute.permitExplicitSet) error("Attribute $attribute does not permit explicit set operations")
        return attribute.kClass.safeCast(when(value) {
            null -> remove(attribute)
            else -> put(attribute as UntypedAttribute, value)
        } ?: defaultValueOf(attribute))
    }

    operator fun <T : Any> set(attribute: Attribute<T>, value: T?) {
        put(attribute, value)
    }

    fun <T : Any> remove(attribute: Attribute<T>): T? {
        return attribute.kClass.safeCast(when(val default = attribute.defaultValue) {
            null -> remove(attribute as UntypedAttribute)
            else -> put(attribute as UntypedAttribute, default())
        } ?: defaultValueOf(attribute))
    }

    inline fun <T : Any, R> update(attribute: Attribute<T>, fn: (T) -> R): R {
        return get(attribute).let { value ->
            try {
                fn(value!!)
            } finally {
                this[attribute] = value
            }
        }
    }
}
