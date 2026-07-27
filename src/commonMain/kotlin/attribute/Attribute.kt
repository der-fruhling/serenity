package net.derfruhling.html.attribute

import net.derfruhling.html.Name
import kotlin.reflect.KClass

abstract class Attribute<T : Any> : UntypedAttribute() {
    abstract override val parser: (String?) -> T?
    abstract val kClass: KClass<T>
}

inline fun <reified T : Any> Attribute(
    name: Name,
    crossinline fn: AttributeBuilder<T>.() -> Unit = {}
) = AttributeBuilder(name, T::class).apply(fn).build()

inline fun <reified T : Any> Attribute(
    name: String,
    crossinline fn: AttributeBuilder<T>.() -> Unit = {}
) = Attribute<T>(Name.of(name), fn)
