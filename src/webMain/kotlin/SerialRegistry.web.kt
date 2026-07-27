@file:OptIn(ExperimentalWasmJsInterop::class)

package net.derfruhling.html

import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

@PublishedApi
internal fun jsonStringify(@Suppress("unused") value: JsAny): String = js("JSON.stringify(value)")

@PublishedApi
internal fun jsonParse(@Suppress("unused") value: String): JsAny = js("JSON.parse(value)")

inline fun <reified T> SerialRegistry.encodeToObject(value: T): JsAny {
    return jsonParse(encode(value))
}

inline fun <reified T> SerialRegistry.decodeFromObject(obj: JsAny): T {
    return decode(jsonStringify(obj))
}

fun SerialRegistry.registerClientPages(fn: PageRegistry.() -> Unit) {
    (object : PageRegistry {
        override fun <T : PageHolder> register(kClass: KClass<T>, kSerializer: KSerializer<T>, page: T) {
            registerPage(kClass, kSerializer)
        }
    }).fn()
}
