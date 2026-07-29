package net.derfruhling.serenity.event

import web.events.EventTarget

actual interface EventTarget {
    val dom: EventTarget
}