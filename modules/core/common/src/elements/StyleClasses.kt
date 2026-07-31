package net.derfruhling.serenity.elements

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

object StyleClasses {
    private val regex = Regex("(?<!^)[A-Z]")

    private fun hyphenate(name: String) = name.replace(regex) { '-' + it.value.lowercase() }

    private val lazyClass
        get() = object : ReadOnlyProperty<Any?, String> {
            private var value: String? = null

            override fun getValue(thisRef: Any?, property: KProperty<*>): String {
                if (value == null) value = "s-${property.name}"
                return value!!
            }
        }

    abstract class Augment(val prefix: String) {
        protected val lazyAugment
            get() = object : ReadOnlyProperty<Any?, String> {
                private var value: String? = null

                override fun getValue(thisRef: Any?, property: KProperty<*>): String {
                    if (value == null) value = prefix + '-' + hyphenate(property.name)
                    return value!!
                }
            }
    }

    val FlexColumn by lazyClass
    val FlexRow by lazyClass
    val PageLayout by lazyClass
    val PageContent by lazyClass

    abstract class Axis(prefix: String) : Augment(prefix) {
        val start by lazyAugment
        val center by lazyAugment
        val end by lazyAugment
    }

    object CrossAxis : Axis("cross") {
        val spaceEvenly by lazyAugment
        val spaceAround by lazyAugment
        val spaceBetween by lazyAugment
    }

    object MainAxis : Axis("main")
}
