package net.derfruhling.html.event

import androidx.compose.runtime.Composable

@Composable
actual inline fun <T> On(type: EventType<T>, crossinline fn: (T) -> Unit) {}
