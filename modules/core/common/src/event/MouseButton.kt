package net.derfruhling.serenity.event

enum class MouseButton(val flag: Int) {
    PRIMARY(MouseButtons.PRIMARY),
    SECONDARY(MouseButtons.SECONDARY),
    MIDDLE(MouseButtons.MIDDLE),
    BACK(MouseButtons.BACK),
    FORWARD(MouseButtons.FORWARD)
}