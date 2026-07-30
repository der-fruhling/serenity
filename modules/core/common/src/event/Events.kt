package net.derfruhling.serenity.event

import kotlinx.datetime.Month
import kotlinx.serialization.Serializable
import net.derfruhling.serenity.annotations.LimitedAvailability
import net.derfruhling.serenity.annotations.Since
import net.derfruhling.serenity.annotations.UnsupportedOnSafari
import net.derfruhling.serenity.annotations.WidelyAvailable
import net.derfruhling.serenity.dom.Window
import net.derfruhling.serenity.tree.platform.Document
import net.derfruhling.serenity.tree.platform.ElementNode

@Serializable
expect sealed interface BuiltinEventType

@Serializable
expect sealed interface BuiltinPlainWindowEvent : BuiltinEventType

@Serializable
expect sealed interface BuiltinPlainDocumentEvent : BuiltinEventType

@Serializable
expect sealed interface BuiltinPointerEvent : BuiltinEventType

@Serializable
expect sealed interface BuiltinPageTransitionEvent : BuiltinEventType

typealias ElementPointerEvent = PointerEvent<ElementNode>

expect fun testSupportedDocumentEvent(name: String): Boolean
expect fun testSupportedWindowEvent(name: String): Boolean

@Serializable
data object BeforeUnloadEvent : EventType<Event<Window>>("beforeunload"), BuiltinPlainWindowEvent

@Serializable
@WidelyAvailable(Since(year = 2021, month = Month.APRIL))
data object VisibilityChangeEvent : EventType<Event<Document>>("visibilitychange"), BuiltinPlainDocumentEvent {
    override val isSupported: Boolean by lazy { testSupportedDocumentEvent(name) }
}

@Serializable
data object PageHideEvent : EventType<PageTransitionEvent>("visibilitychange"), BuiltinPageTransitionEvent

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
