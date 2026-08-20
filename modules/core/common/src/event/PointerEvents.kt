package net.derfruhling.serenity.event

import kotlinx.datetime.Month
import kotlinx.serialization.Serializable
import net.derfruhling.serenity.annotations.LimitedAvailability
import net.derfruhling.serenity.annotations.Since
import net.derfruhling.serenity.annotations.UnsupportedOnSafari
import net.derfruhling.serenity.annotations.WidelyAvailable
import net.derfruhling.serenity.dom.Element

typealias ElementPointerEvent = PointerEvent<Element>

@WidelyAvailable(Since(year = 2020, Month.JULY))
object PointerEvents {
    @Serializable
    data object Over : EventType<ElementPointerEvent>("pointerover"), BuiltinPointerEvent

    @Serializable
    data object Enter : EventType<ElementPointerEvent>("pointerenter"), BuiltinPointerEvent

    @Serializable
    data object Down : EventType<ElementPointerEvent>("pointerdown"), BuiltinPointerEvent

    @Serializable
    data object Move : EventType<ElementPointerEvent>("pointermove"), BuiltinPointerEvent

    @Serializable
    data object Up : EventType<ElementPointerEvent>("pointerup"), BuiltinPointerEvent

    @Serializable
    data object Cancel : EventType<ElementPointerEvent>("pointercancel"), BuiltinPointerEvent

    @Serializable
    data object Out : EventType<ElementPointerEvent>("pointerout"), BuiltinPointerEvent

    @Serializable
    data object Leave : EventType<ElementPointerEvent>("pointerleave"), BuiltinPointerEvent

    @Serializable
    data object GotCapture : EventType<ElementPointerEvent>("gotpointercapture"),
                             BuiltinPointerEvent

    @Serializable
    data object LostCapture : EventType<ElementPointerEvent>("lostpointercapture"),
                              BuiltinPointerEvent

    @LimitedAvailability
    @UnsupportedOnSafari
    @Serializable
    data object RawUpdate : EventType<ElementPointerEvent>("pointerrawupdate"), BuiltinPointerEvent
}
