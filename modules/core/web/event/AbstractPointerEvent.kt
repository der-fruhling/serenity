@file:OptIn(ExperimentalWasmJsInterop::class)

package net.derfruhling.serenity.event

import js.array.toList
import net.derfruhling.serenity.annotations.NewWebApi
import net.derfruhling.serenity.annotations.UnsupportedOnSafari
import net.derfruhling.serenity.dom.Element
import net.derfruhling.serenity.takeIfPresent
import web.pointer.PointerEvent as DomPointerEvent

abstract class AbstractPointerEvent<T : EventTarget>(dom: DomPointerEvent) : AbstractMouseEvent<T>(
    dom
),
                                                                             PointerEvent<T> {
    @NewWebApi
    override val altitudeAngle: Float? by lazy {
        dom.takeIfPresent(DomPointerEvent::altitudeAngle)
            ?.toFloat()
    }

    @NewWebApi
    override val azimuthAngle: Float? by lazy {
        dom.takeIfPresent(DomPointerEvent::azimuthAngle)
            ?.toFloat()
    }
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

    internal class ElementImpl(override val dom: DomPointerEvent) : AbstractPointerEvent<Element>(
        dom
    ) {
        override fun eventTargetFromDom(eventTarget: web.events.EventTarget?): Element {
            return eventTarget!! as Element
        }

        override fun getCoalescedEvents(): List<PointerEvent<Element>> {
            return dom.takeIfPresent("getCoalescedEvents") {
                dom.getCoalescedEvents().toList().map { ElementImpl(it) }
            } ?: emptyList()
        }

        @NewWebApi
        override fun getPredictedEvents(): List<PointerEvent<Element>> {
            return dom.takeIfPresent("getPredictedEvents") {
                dom.getPredictedEvents().toList().map { ElementImpl(it) }
            } ?: emptyList()
        }
    }
}

fun DomPointerEvent.asComposeEvent(): PointerEvent<Element> =
    AbstractPointerEvent.ElementImpl(this)
