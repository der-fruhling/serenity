package net.derfruhling.serenity.attribute

private val setParserRegex by lazy { Regex("\\s+") }

abstract class AbstractAttributeContainer {
    protected val map = mutableMapOf<String, Lazy<UntypedAttribute>>()

    protected fun <T : Any> register(name: String, lazy: Lazy<Attribute<T>>) =
        lazy.also { map[name] = it }

    protected inline fun <reified T : Any> name(name: String): Lazy<Attribute<T>> =
        register(name, lazy { Attribute<T>(name) })

    protected inline fun <reified T : Any> name(
        name: String,
        crossinline fn: AttributeBuilder<T>.() -> Unit
    ): Lazy<Attribute<T>> =
        register(name, lazy { Attribute<T>(name, fn) })

    protected fun AttributeBuilder<MutableSet<String>>.stringSet() {
        permitExplicitSet = false

        defaultValue { mutableSetOf() }
        parser { it?.split(setParserRegex)?.toMutableSet() }
    }

    operator fun get(name: String): Lazy<UntypedAttribute>? {
        return map[name]
    }
}