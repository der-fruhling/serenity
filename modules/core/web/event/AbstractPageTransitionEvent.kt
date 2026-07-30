package net.derfruhling.serenity.event

import net.derfruhling.serenity.dom.Window
import web.events.Event
import web.events.EventTarget
import web.window.Window as DomWindow
import web.history.PageTransitionEvent as DomPageTransitionEvent

abstract class AbstractPageTransitionEvent(dom: Event) : AbstractEvent<Window>(dom), PageTransitionEvent {
    override fun eventTargetFromDom(eventTarget: EventTarget?): Window {
        return object : Window {
            override val dom: DomWindow = eventTarget as DomWindow
        }
    }

    class WindowImpl(override val dom: DomPageTransitionEvent) : AbstractPageTransitionEvent(dom) {
        override val persisted: Boolean by dom::persisted
    }
}

fun DomPageTransitionEvent.asWindowComposeEvent(): PageTransitionEvent =
    AbstractPageTransitionEvent.WindowImpl(this)
