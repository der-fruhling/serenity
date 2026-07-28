package net.derfruhling.serenity.event

import kotlin.jvm.JvmInline

@JvmInline
value class MouseButtons(val buttons: Int) {
    val isPrimary: Boolean get() = (buttons and PRIMARY) != 0
    val isSecondary: Boolean get() = (buttons and SECONDARY) != 0
    val isMiddle: Boolean get() = (buttons and MIDDLE) != 0
    val isBack: Boolean get() = (buttons and BACK) != 0
    val isForward: Boolean get() = (buttons and FORWARD) != 0

    companion object {
        const val PRIMARY: Int = 1 shl 0
        const val SECONDARY: Int = 1 shl 1
        const val MIDDLE: Int = 1 shl 2
        const val BACK: Int = 1 shl 3
        const val FORWARD: Int = 1 shl 4
    }
}