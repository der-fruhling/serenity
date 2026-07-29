@file:OptIn(ExperimentalWasmJsInterop::class)

package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.KSerializer
import net.derfruhling.serenity.elements.HeadContext
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

internal var headBuilder by mutableStateOf(null as (@Composable HeadContext.() -> Unit)?)

fun SerialRegistry.registerClientPages(fn: PageRegistry.() -> Unit) {
    (object : PageRegistry {
        override fun head(fn: @Composable (HeadContext.() -> Unit)) {
            headBuilder = fn
        }

        override fun <T : PageHolder> register(kClass: KClass<T>, kSerializer: KSerializer<T>, page: T) {
            registerPage(kClass, kSerializer)
        }
    }).fn()
}
