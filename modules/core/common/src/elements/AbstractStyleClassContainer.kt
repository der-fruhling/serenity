package net.derfruhling.serenity.elements

import net.derfruhling.serenity.DelegateProvider
import kotlin.properties.ReadOnlyProperty

private val regex = Regex("(?<!^)[A-Z]")

internal fun hyphenate(name: String) = name.replace(regex) { '-' + it.value.lowercase() }

abstract class AbstractStyleClassContainer {
    protected open val lazyClass =
        DelegateProvider<Any?, ReadOnlyProperty<Any?, String>> { _, property ->
            val name = hyphenate(property.name)
            ReadOnlyProperty { _, _ -> name }
        }
}
