@file:OptIn(ExperimentalWasmJsInterop::class)

package net.derfruhling.serenity.serial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.KSerializer
import net.derfruhling.serenity.PageContext
import net.derfruhling.serenity.PageHolder
import net.derfruhling.serenity.PageHolderFactory
import net.derfruhling.serenity.PageRegistry
import net.derfruhling.serenity.PageTemplate
import net.derfruhling.serenity.TemplateBuilder
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

internal var pageTemplate by mutableStateOf(null as PageTemplate?)

fun SerialRegistry.registerClientPages(fn: PageRegistry.() -> Unit) {
    (object : PageRegistry() {
        override fun template(fn: @Composable (TemplateBuilder.() -> Unit)) {
            pageTemplate = PageTemplate(fn)
        }

        override fun <R : PageHolder<R>, T : PageHolderFactory<PageContext, R>> register(
            kClass: KClass<R>,
            kSerializer: KSerializer<R>,
            page: T
        ) {
            registerPage(kClass, kSerializer)
        }
    }).fn()
}
