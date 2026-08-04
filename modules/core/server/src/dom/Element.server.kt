package net.derfruhling.serenity.dom

import net.derfruhling.serenity.event.EventTarget
import net.derfruhling.serenity.tree.platform.ElementNode

actual class Element(val real: ElementNode) : EventTarget()

actual val Element.node: ElementNode
    get() = real
