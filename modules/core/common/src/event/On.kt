package net.derfruhling.serenity.event

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import net.derfruhling.serenity.annotations.Client
import net.derfruhling.serenity.annotations.HtmlComposable
import kotlin.coroutines.CoroutineContext

expect class EventContext : CoroutineScope {
    override val coroutineContext: CoroutineContext
}

@Composable
@HtmlComposable
expect fun <T> On(type: EventType<T>, fn: @Client @HtmlComposable EventContext.(T) -> Unit)
