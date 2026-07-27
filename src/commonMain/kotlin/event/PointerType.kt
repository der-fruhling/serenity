package net.derfruhling.html.event

import kotlinx.datetime.Month
import net.derfruhling.html.annotations.Since
import net.derfruhling.html.annotations.WidelyAvailable

@WidelyAvailable(Since(year = 2020, Month.JULY))
sealed class PointerType(private val text: String) {
    data object Mouse : PointerType("mouse")
    data object Pen : PointerType("pen")
    data object Touch : PointerType("touch")
    data class Unknown(val text: String) : PointerType(text)

    override fun toString(): String {
        return "PointerType[$text]"
    }

    companion object {
        fun fromString(string: String) = when(string) {
            "mouse" -> Mouse
            "pen" -> Pen
            "touch" -> Touch
            else -> Unknown(string)
        }
    }
}
