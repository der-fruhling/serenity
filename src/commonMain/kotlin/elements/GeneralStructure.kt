package net.derfruhling.html.elements

import net.derfruhling.html.attribute.Attributes
import net.derfruhling.html.attribute.StringSetComposableAttribute

abstract class GeneralStructure {
    abstract class Attr {
        val classes = StringSetComposableAttribute(Attributes.`class`)
    }
}
