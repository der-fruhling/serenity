package net.derfruhling.serenity.dom

import net.derfruhling.serenity.tree.platform.ElementNode

actual typealias Element = web.dom.Element
actual typealias DomValidityState = web.validation.ValidityState
actual typealias DomValidationTarget = web.validation.ValidationTarget
actual typealias HTMLSelectElement = web.html.HTMLSelectElement

actual val Element.node: ElementNode
    get() = ElementNode.tryGet(this)

