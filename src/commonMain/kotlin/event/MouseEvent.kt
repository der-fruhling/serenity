package net.derfruhling.html.event

import net.derfruhling.html.annotations.NewWebApi

interface MouseEvent<T : EventTarget> : UIEvent<T> {
    val altKey: Boolean
    val button: MouseButton
    val buttons: MouseButtons?
    val clientX: Int
    val clientY: Int
    val ctrlKey: Boolean
    val metaKey: Boolean
    @NewWebApi
    val movementX: Double?
    @NewWebApi
    val movementY: Double?
    val offsetX: Double
    val offsetY: Double
    val pageX: Double
    val pageY: Double
    val relatedTarget: T?
    val screenX: Double
    val screenY: Double
    val shiftKey: Boolean

    val x: Int get() = clientX
    val y: Int get() = clientY
}