package net.derfruhling.serenity.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import kotlinx.coroutines.CoroutineScope
import net.derfruhling.serenity.HtmlComposable
import net.derfruhling.serenity.platform.EventHandlerNode
import net.derfruhling.serenity.platform.HtmlApplier
import kotlin.coroutines.CoroutineContext

actual class EventContext private constructor() : CoroutineScope {
    actual override val coroutineContext: CoroutineContext
        get() = throw NotImplementedError()
}

@Composable
@HtmlComposable
actual fun <T : Event<*>> On(type: EventType<T>, fn: Handler<T>) {
    ComposeNode<EventHandlerNode<T>, HtmlApplier>(::EventHandlerNode, update = {
        set(type) { this.type = type }
    })
}
