package net.derfruhling.serenity

import kotlin.reflect.KProperty1

@OptIn(ExperimentalWasmJsInterop::class)
@PublishedApi
internal fun isUndefined(value: JsAny): Boolean = js("value === undefined")

@OptIn(ExperimentalWasmJsInterop::class)
@PublishedApi
internal fun isNull(value: JsAny): Boolean = js("value === null")

@ExperimentalWasmJsInterop
private fun checkField(of: JsAny, name: String): Boolean = js("name in of")

@ExperimentalWasmJsInterop
fun <T : JsAny> T.hasField(name: String): Boolean = checkField(this, name)

@ExperimentalWasmJsInterop
inline fun <T : JsAny, R> T.takeIfPresent(name: String, fn: (T) -> R): R? = if(this.hasField(name)) {
    fn(this)
} else {
    null
}

@ExperimentalWasmJsInterop
fun <T : JsAny, R> T.takeIfPresent(property: KProperty1<T, R>): R? = if(this.hasField(property.name)) {
    property.get(this)
} else {
    null
}
