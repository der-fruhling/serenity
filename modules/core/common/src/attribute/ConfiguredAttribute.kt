package net.derfruhling.serenity.attribute

import net.derfruhling.serenity.Name
import kotlin.reflect.KClass

data class ConfiguredAttribute<T : Any>(
    override val name: Name,
    override val parser: (String?) -> T?,
    override val kClass: KClass<T>,
    override val permitExplicitSet: Boolean,
    override val defaultValue: (() -> Any?)?
) : Attribute<T>()
