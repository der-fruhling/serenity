package net.derfruhling.html.tree.platform

import net.derfruhling.html.Name
import net.derfruhling.html.attribute.Attribute

abstract class Registry<in R, out T>(val namer: Namer<R>, val default: (R) -> T) {
    fun interface Namer<in R> {
        fun nameOf(value: R): Name
    }

    private val simpleNames = mutableMapOf<Name, (R) -> T>()
    private val matchers = mutableListOf<Pair<Matcher<R>, (R) -> T>>()

    fun <U : @UnsafeVariance T> register(
        matcher: Matcher<@UnsafeVariance R>,
        builder: (@UnsafeVariance R) -> U
    ) {
        matchers.add(matcher to builder)
    }

    fun <U : @UnsafeVariance T> register(name: Name, builder: (@UnsafeVariance R) -> U) {
        simpleNames.put(name, builder) ?: throw IllegalStateException("cannot override registered elements")
    }

    fun derive(realElement: R): T {
        for((matcher, factory) in matchers) {
            if(matcher.match(realElement)) return factory(realElement)
        }

        return simpleNames[namer.nameOf(realElement)]?.invoke(realElement) ?: default(realElement)
    }
}

object ElementRegistry : Registry<RealElement, ElementNode>(RealElementNamer, ::ElementNode)
object AttributeRegistry : Registry<RealAttribute, Attribute<*>>(RealAttributeNamer, { name -> Attribute<String>(name.name) })
