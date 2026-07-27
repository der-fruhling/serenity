package net.derfruhling.html.event

import androidx.compose.runtime.Composable

@Composable
expect fun <T> On(type: EventType<T>, fn: (T) -> Unit)
