package net.derfruhling.serenity.elements

import net.derfruhling.serenity.DelegateProvider
import kotlin.properties.ReadOnlyProperty

abstract class AbstractPrefixedStyleClassContainer(val prefix: String, val hyphenate: Boolean = true) : AbstractStyleClassContainer() {
    override val lazyClass = DelegateProvider<Any?, ReadOnlyProperty<Any?, String>> { _, property ->
        val name = prefix + '-' + if(hyphenate) hyphenate(property.name) else property.name
        ReadOnlyProperty { _, _ -> name }
    }
}
