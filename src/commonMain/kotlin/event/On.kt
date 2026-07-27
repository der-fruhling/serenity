package net.derfruhling.html.event

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

expect class EventContext : CoroutineScope {
    override val coroutineContext: CoroutineContext
}

@Composable
expect fun <T> On(type: EventType<T>, fn: EventContext.(T) -> Unit)
