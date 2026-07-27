@file:OptIn(ExperimentalWasmJsInterop::class)

package net.derfruhling.html.event

import js.numbers.JsNumbers.toKotlinDouble
import js.string.JsStrings.toKotlinString
import net.derfruhling.html.tree.platform.ElementNode
import web.dom.Element as DomElement
import web.events.Event as DomEvent
import web.events.EventTarget as DomEventTarget

abstract class AbstractEvent<T: EventTarget>(dom: DomEvent) : Event<T> {
    abstract val dom: DomEvent

    override val target: T by lazy { eventTargetFromDom(dom.target) }
    override val currentTarget: T by lazy { eventTargetFromDom(dom.currentTarget) }
    override val bubbles: Boolean by dom::bubbles
    override val cancelable: Boolean by dom::cancelable
    override val composed: Boolean by dom::composed
    override val defaultPrevented: Boolean by dom::defaultPrevented
    override val isTrusted: Boolean by dom::isTrusted
    override val timeStamp: Double by lazy { dom.timeStamp.toKotlinDouble() }
    override val type: String by lazy { dom.type.unsafeCast<JsString>().toKotlinString() }

    override fun preventDefault() = dom.preventDefault()
    override fun stopImmediatePropagation() = dom.stopImmediatePropagation()
    override fun stopPropagation() = dom.stopPropagation()

    abstract fun eventTargetFromDom(eventTarget: DomEventTarget?): T

    companion object {
        fun elementFromDom(eventTarget: DomEventTarget): ElementNode =
            ElementNode.tryGet(eventTarget as DomElement)
    }
}
