@file:OptIn(ExperimentalWasmJsInterop::class)

package net.derfruhling.serenity.event

import net.derfruhling.serenity.annotations.NewWebApi
import net.derfruhling.serenity.hasField
import net.derfruhling.serenity.takeIfPresent
import web.mouse.*
import web.mouse.MouseButton as DomMouseButton
import web.mouse.MouseEvent as DomMouseEvent
import web.mouse.MouseButtons as DomMouseButtons

private fun mouseButtonFromDom(dom: DomMouseButton): MouseButton = when (dom) {
    DomMouseButton.MAIN -> MouseButton.PRIMARY
    DomMouseButton.SECONDARY -> MouseButton.SECONDARY
    DomMouseButton.AUXILIARY -> MouseButton.MIDDLE
    DomMouseButton.FOURTH -> MouseButton.BACK
    DomMouseButton.FIFTH -> MouseButton.FORWARD
}

private fun mouseButtonsFromDom(dom: DomMouseButtons): MouseButtons {
    return MouseButtons(dom.unsafeCast<JsNumber>().toInt())
}

private external interface DomMouseEventExt {
    val screenX: Double
    val screenY: Double
}

abstract class AbstractMouseEvent<T: EventTarget>(dom: DomMouseEvent) : AbstractUIEvent<T>(dom), MouseEvent<T> {
    override val altKey: Boolean by dom::altKey
    override val button: MouseButton by lazy { mouseButtonFromDom(dom.button) }
    override val buttons: MouseButtons? by lazy { dom.takeIfPresent("buttons") { mouseButtonsFromDom(dom.buttons) } }
    override val clientX: Int by dom::clientX
    override val clientY: Int by dom::clientY
    override val ctrlKey: Boolean by dom::ctrlKey
    override val metaKey: Boolean by dom::metaKey

    @NewWebApi
    override val movementX: Double? by lazy { if (dom.hasField("movementX")) dom.movementX else null }

    @NewWebApi
    override val movementY: Double? by lazy { if (dom.hasField("movementY")) dom.movementY else null }

    override val offsetX: Double by dom::offsetX
    override val offsetY: Double by dom::offsetY
    override val pageX: Double by dom::pageX
    override val pageY: Double by dom::pageY
    override val relatedTarget: T? by lazy { dom.relatedTarget?.let { eventTargetFromDom(it) } }
    override val screenX: Double by (dom as DomMouseEventExt)::screenX
    override val screenY: Double by (dom as DomMouseEventExt)::screenY
    override val shiftKey: Boolean by dom::shiftKey
}
