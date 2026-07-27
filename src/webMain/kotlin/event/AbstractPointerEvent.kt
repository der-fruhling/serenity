@file:OptIn(ExperimentalWasmJsInterop::class)

package net.derfruhling.html.event

import js.array.toList
import net.derfruhling.html.annotations.NewWebApi
import net.derfruhling.html.annotations.UnsupportedOnSafari
import net.derfruhling.html.takeIfPresent
import net.derfruhling.html.tree.platform.ElementNode
import web.pointer.PointerEvent as DomPointerEvent

abstract class AbstractPointerEvent<T: EventTarget>(dom: DomPointerEvent) : AbstractMouseEvent<T>(dom), PointerEvent<T> {
    @NewWebApi
    override val altitudeAngle: Float? by lazy { dom.takeIfPresent(DomPointerEvent::altitudeAngle)?.toFloat() }

    @NewWebApi
    override val azimuthAngle: Float? by lazy { dom.takeIfPresent(DomPointerEvent::azimuthAngle)?.toFloat() }
    override val width: Double by dom::width
    override val height: Double by dom::height
    override val isPrimary: Boolean by dom::isPrimary

    @UnsupportedOnSafari
    override val persistentDeviceId: Int? by lazy { dom.takeIfPresent(DomPointerEvent::persistentDeviceId) }
    override val pointerId: Int by dom::pointerId
    override val pointerType: PointerType by lazy { PointerType.fromString(dom.pointerType) }
    override val pressure: Float by dom::pressure
    override val tangentialPressure: Float by dom::tangentialPressure
    override val tiltX: Int by dom::tiltX
    override val tiltY: Int by dom::tiltY
    override val twist: Int by dom::twist

    internal class ElementImpl(override val dom: DomPointerEvent) : AbstractPointerEvent<ElementNode>(dom) {
        override fun eventTargetFromDom(eventTarget: web.events.EventTarget?): ElementNode {
            return elementFromDom(eventTarget!!)
        }

        override fun getCoalescedEvents(): List<PointerEvent<ElementNode>> {
            return dom.takeIfPresent("getCoalescedEvents") {
                dom.getCoalescedEvents().toList().map { ElementImpl(it) }
            } ?: emptyList()
        }

        @NewWebApi
        override fun getPredictedEvents(): List<PointerEvent<ElementNode>> {
            return dom.takeIfPresent("getPredictedEvents") {
                dom.getPredictedEvents().toList().map { ElementImpl(it) }
            } ?: emptyList()
        }
    }
}

fun DomPointerEvent.asComposeEvent(): PointerEvent<ElementNode> =
    AbstractPointerEvent.ElementImpl(this)
