package net.derfruhling.serenity.modularity.extension

import androidx.compose.runtime.*
import net.derfruhling.serenity.PageHolder
import kotlin.reflect.KClass

@Stable
class PageExtensionController {
    private var extensions by mutableStateOf(emptyMap<KClass<out Annotation>, AbstractPageExtension>())

    fun setPage(page: PageHolder<*>) {
        extensions = page.extensions
    }

    @Composable
    fun with(fn: @Composable PageExtensionPoints.() -> Unit) {
        val ext = remember(extensions) {
            val instance = PageExtensionInstance()

            for ((_, value) in extensions) {
                value.initialize(instance)
            }

            instance
        }

        ext.fn()
        ext.reset()
    }

    companion object {
        @Composable
        fun with(fn: @Composable PageExtensionPoints.() -> Unit) {
            pageExtensionControllerLocal.current.with(fn)
        }
    }
}

val pageExtensionControllerLocal =
    staticCompositionLocalOf<PageExtensionController> { throw NotImplementedError() }
