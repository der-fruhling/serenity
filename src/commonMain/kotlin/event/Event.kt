package net.derfruhling.html.event

import kotlinx.datetime.Month
import net.derfruhling.html.annotations.Since
import net.derfruhling.html.annotations.WidelyAvailable

@WidelyAvailable
interface Event<T: EventTarget> {
    val bubbles: Boolean
    val cancelable: Boolean
    @WidelyAvailable(Since(year = 2020, Month.JANUARY))
    val composed: Boolean
    val currentTarget: T
    val defaultPrevented: Boolean
    @WidelyAvailable(Since(year = 2016, Month.SEPTEMBER))
    val isTrusted: Boolean
    val target: T
    val timeStamp: Double
    val type: String

    fun preventDefault()
    fun stopImmediatePropagation()
    fun stopPropagation()
}
