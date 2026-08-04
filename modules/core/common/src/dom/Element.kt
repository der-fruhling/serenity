package net.derfruhling.serenity.dom

import net.derfruhling.serenity.event.EventTarget
import net.derfruhling.serenity.tree.platform.ElementNode

expect class Element : EventTarget {
}

expect val Element.node: ElementNode
