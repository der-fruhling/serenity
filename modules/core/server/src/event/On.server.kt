package net.derfruhling.serenity.event

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import net.derfruhling.serenity.annotations.Client
import net.derfruhling.serenity.HtmlComposable
import kotlin.coroutines.CoroutineContext

actual class EventContext private constructor() : CoroutineScope {
    actual override val coroutineContext: CoroutineContext
        get() = throw NotImplementedError()
}

@Composable
@HtmlComposable
actual inline fun <T> On(type: EventType<T>, crossinline fn: @Client @HtmlComposable EventContext.(T) -> Unit) {
}
