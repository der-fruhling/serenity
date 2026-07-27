package net.derfruhling.html.event

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import net.derfruhling.html.annotations.Client
import kotlin.coroutines.CoroutineContext

@Composable
actual inline fun <T> On(type: EventType<T>, crossinline fn: @Client EventContext.(T) -> Unit) {}

actual class EventContext private constructor() : CoroutineScope {
    actual override val coroutineContext: CoroutineContext
        get() = throw NotImplementedError()
}