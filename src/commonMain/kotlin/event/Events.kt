package net.derfruhling.html.event

import kotlinx.datetime.Month
import kotlinx.serialization.Serializable
import net.derfruhling.html.annotations.LimitedAvailability
import net.derfruhling.html.annotations.Since
import net.derfruhling.html.annotations.UnsupportedOnSafari
import net.derfruhling.html.annotations.WidelyAvailable
import net.derfruhling.html.tree.platform.ElementNode

@Serializable
expect sealed interface BuiltinEventType

@Serializable
expect sealed interface BuiltinPointerEvent

typealias ElementPointerEvent = PointerEvent<ElementNode>

@Serializable
data object ClickEvent : EventType<ElementPointerEvent>("click"), BuiltinPointerEvent {
    @Serializable
    data object Double : EventType<ElementPointerEvent>("dblclick"), BuiltinPointerEvent
}

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
    data object GotCapture : EventType<ElementPointerEvent>("gotpointercapture"), BuiltinPointerEvent

    @Serializable
    data object LostCapture : EventType<ElementPointerEvent>("lostpointercapture"), BuiltinPointerEvent

    @LimitedAvailability
    @UnsupportedOnSafari
    @Serializable
    data object RawUpdate : EventType<ElementPointerEvent>("pointerrawupdate"), BuiltinPointerEvent
}
