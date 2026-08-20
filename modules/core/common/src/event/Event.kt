package net.derfruhling.serenity.event

import kotlinx.datetime.Month
import net.derfruhling.serenity.annotations.Client
import net.derfruhling.serenity.annotations.Since
import net.derfruhling.serenity.annotations.WidelyAvailable
import net.derfruhling.serenity.dom.Document
import net.derfruhling.serenity.dom.Element
import net.derfruhling.serenity.dom.EventTarget
import net.derfruhling.serenity.dom.Window

@WidelyAvailable
interface Event<T : EventTarget> {
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

typealias ElementEvent = Event<Element>
typealias DocumentEvent = Event<Document>
typealias WindowEvent = Event<Window>

typealias Handler<T> = @Client EventContext.(T) -> Unit
