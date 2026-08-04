package net.derfruhling.serenity.dom

import net.derfruhling.serenity.event.EventTarget
import net.derfruhling.serenity.tree.platform.ElementNode

actual typealias Element = web.dom.Element

actual val Element.node: ElementNode
    get() = ElementNode.tryGet(this)
