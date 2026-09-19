@file:OptIn(ExperimentalWasmJsInterop::class)

package net.derfruhling.serenity.serial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

internal var pageTemplate by mutableStateOf(null as net.derfruhling.serenity.PageTemplate?)

interface WebContext {
    fun parseParameters(path: String): Map<String, String>
}

fun SerialRegistry.registerClientPages(fn: net.derfruhling.serenity.PageRegistry<WebContext>.() -> Unit) {
    (object : net.derfruhling.serenity.PageRegistry<WebContext>() {
        override fun template(fn: @Composable (net.derfruhling.serenity.TemplateBuilder.() -> Unit)) {
            pageTemplate = _root_ide_package_.net.derfruhling.serenity.PageTemplate(fn)
        }

        override fun <R : net.derfruhling.serenity.PageHolder<R>, T : net.derfruhling.serenity.PageHolderFactory<WebContext, R>> register(
            kClass: KClass<R>,
            kSerializer: KSerializer<R>,
            page: T
        ) {
            registerPage(kClass, kSerializer)
        }
    }).fn()
}
