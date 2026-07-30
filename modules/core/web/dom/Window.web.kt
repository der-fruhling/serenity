package net.derfruhling.serenity.dom

import net.derfruhling.serenity.event.EventTarget
import web.window.Window
import web.window.window

actual interface Window : EventTarget {
    override val dom: Window
}