package net.derfruhling.serenity.event

import net.derfruhling.serenity.dom.Window

interface PageTransitionEvent : Event<Window> {
    val persisted: Boolean
}
