package net.derfruhling.serenity

import js.errors.JsError
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
inline fun <T : JsAny, R> T.takeIfPresent(name: String, fn: (T) -> R): R? =
    if (this.hasField(name)) {
        fn(this)
    } else {
        null
    }

@ExperimentalWasmJsInterop
fun <T : JsAny, R> T.takeIfPresent(property: KProperty1<T, R>): R? =
    if (this.hasField(property.name)) {
        property.get(this)
    } else {
        null
    }

sealed class JsResult<T> {
    data class Success<T>(val value: T) : JsResult<T>()
    data class Failure<T>(val error: JsError) : JsResult<T>()
}

private external interface JsTryCatchResult<R : JsAny> {
    val success: R?
    val error: JsError?
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun <R : JsAny> jsTryCatch(
    lambda: () -> R?
): JsTryCatchResult<R> = js("""(() => {
    try {
        return { success: lambda() };
    } catch(error) {
        return { error }
    }
})()""")

@OptIn(ExperimentalWasmJsInterop::class)
fun <R> catch(fn: () -> R): JsResult<R> {
    val result = jsTryCatch { fn()?.toJsReference() }

    @Suppress("UNCHECKED_CAST")
    return when {
        result.success != null -> JsResult.Success(result.success?.get() as R)
        else -> JsResult.Failure(result.error!!)
    }
}
