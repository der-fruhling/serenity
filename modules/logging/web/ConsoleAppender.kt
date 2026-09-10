package net.derfruhling.serenity.logging

import io.github.oshai.kotlinlogging.Appender
import io.github.oshai.kotlinlogging.KLoggingEvent
import io.github.oshai.kotlinlogging.Level
import js.errors.JsError
import js.errors.toJsErrorLike
import web.console.console
import web.dom.Node

@OptIn(ExperimentalWasmJsInterop::class)
class ConsoleAppender : Appender {
    override fun log(loggingEvent: KLoggingEvent) {
        val args = mutableListOf<JsAny>()
        loggingEvent.message?.let { args.add(it.toJsString()) }

        loggingEvent.payload?.let { map ->
            val element by map

            if(element != null && element is Node) {
                args.add(element as Node)
            }
        }

        loggingEvent.cause?.let {
            // JsException is a typealias to Throwable on JS, but not Wasm/JS
            @Suppress("USELESS_IS_CHECK")
            if(it is JsException) {
                args.add(it.thrownValue!!)
            } else {
                args.add(it.toJsErrorLike() ?: ("\n" + it.stackTraceToString()).toJsString())
            }
        }

        when(loggingEvent.level) {
            Level.TRACE -> console.debug(*arrayOf<JsAny>("[trace]".toJsString()) + args.toTypedArray())
            Level.DEBUG -> console.debug(*args.toTypedArray())
            Level.INFO -> console.info(*args.toTypedArray())
            Level.WARN -> console.warn(*args.toTypedArray())
            Level.ERROR -> console.error(*args.toTypedArray())
            Level.OFF -> {}
        }
    }
}
