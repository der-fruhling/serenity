package net.derfruhling.serenity.attribute

import net.derfruhling.serenity.elements.form.FormEncoding
import net.derfruhling.serenity.elements.form.FormMethod

@Suppress("ObjectPropertyName")
object HtmlAttributes : AbstractAttributeContainer() {
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
    val placeholder by name<String>("placeholder")
}
