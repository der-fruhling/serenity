package net.derfruhling.html.event

import web.events.EventTarget

actual interface EventTarget {
    val dom: EventTarget
}