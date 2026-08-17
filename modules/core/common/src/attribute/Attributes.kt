package net.derfruhling.serenity.attribute

import net.derfruhling.serenity.elements.FormEncoding
import net.derfruhling.serenity.elements.FormMethod

private val setParserRegex by lazy { Regex("\\s+") }

@Suppress("ObjectPropertyName")
object Attributes {
    private val map = mutableMapOf<String, Lazy<UntypedAttribute>>()

    private fun <T : Any> register(name: String, lazy: Lazy<Attribute<T>>) =
        lazy.also { map[name] = it }

    private inline fun <reified T : Any> name(name: String): Lazy<Attribute<T>> =
        register(name, lazy { Attribute<T>(name) })

    private inline fun <reified T : Any> name(
        name: String,
        crossinline fn: AttributeBuilder<T>.() -> Unit
    ): Lazy<Attribute<T>> =
        register(name, lazy { Attribute<T>(name, fn) })

    private fun AttributeBuilder<MutableSet<String>>.stringSet() {
        permitExplicitSet = false

        defaultValue { mutableSetOf<String>() }
        parser { it?.split(setParserRegex)?.toMutableSet() }
    }

    val type by name<String>("type")
    val lang by name<String>("lang")
    val src by name<String>("src")
    val async by name<Boolean>("async")
    val defer by name<Boolean>("defer")
    val href by name<String>("href")
    val rel by name<String>("rel")
    val `as` by name<String>("as")
    val target by name<String>("target")
    val id by name<String>("id")
    val `class` by name<MutableSet<String>>("class") { stringSet() }
    val style by name<String>("style")
    val name by name<String>("name")
    val label by name<String>("label")
    val value by name<String>("value")
    val multiple by name<Boolean>("multiple")
    val autofocus by name<Boolean>("autofocus")
    val disabled by name<Boolean>("disabled")
    val form by name<String>("form")
    val selected by name<Boolean>("selected")
    val `accept-charset` by name<String>("accept-charset")
    val action by name<String>("action")
    val autocomplete by name<Boolean>("autocomplete")
    val enctype by name<FormEncoding>("enctype")
    val method by name<FormMethod>("method")
    val novalidate by name<Boolean>("novalidate")

    val serenityKeepIfRemoved by name<Boolean>("data-keep-if-removed")

    operator fun get(name: String): Lazy<UntypedAttribute>? {
        return map[name]
    }
}