@file:OptIn(ExperimentalWasmJsInterop::class)

package net.derfruhling.serenity.event

import kotlinx.serialization.Serializable
import net.derfruhling.serenity.dom.Element
import net.derfruhling.serenity.dom.Window
import net.derfruhling.serenity.dom.Document
import web.events.Event as DomEvent
import web.history.PageTransitionEvent as DomPageTransitionEvent
import web.pointer.PointerEvent as DomPointerEvent

@Serializable
actual sealed interface BuiltinEventType

actual fun testSupportedDocumentEvent(name: String): Boolean = js("('on' + name) in document")
actual fun testSupportedWindowEvent(name: String): Boolean = js("('on' + name) in window")

@Serializable
actual sealed interface BuiltinPointerEvent : BuiltinEventType,
                                              GenerateFromDomEvent<PointerEvent<Element>> {
    override fun generate(event: DomEvent): PointerEvent<Element> {
        return (event as DomPointerEvent).asComposeEvent()
    }
}

@Serializable
actual sealed interface BuiltinPlainElementEvent : BuiltinEventType, GenerateFromDomEvent<Event<Element>> {
    override fun generate(event: DomEvent): Event<Element> {
        return event.asElementComposeEvent()
    }
}

@Serializable
actual sealed interface BuiltinPlainWindowEvent : BuiltinEventType,
                                                  GenerateFromDomEvent<Event<Window>> {
    override fun generate(event: DomEvent): Event<Window> {
        return event.asWindowComposeEvent()
    }
}

@Serializable
actual sealed interface BuiltinPlainDocumentEvent : BuiltinEventType,
                                                    GenerateFromDomEvent<Event<Document>> {
    override fun generate(event: DomEvent): Event<Document> {
        return event.asDocumentComposeEvent()
    }
}

@Serializable
actual sealed interface BuiltinPageTransitionEvent : BuiltinEventType,
                                                     GenerateFromDomEvent<PageTransitionEvent> {
    override fun generate(event: DomEvent): PageTransitionEvent {
        return (event as DomPageTransitionEvent).asWindowComposeEvent()
    }
}