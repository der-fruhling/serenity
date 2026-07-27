package net.derfruhling.html.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import net.derfruhling.html.annotations.HtmlComposable

@Suppress("NOTHING_TO_INLINE")
@Composable
@HtmlComposable
@ReadOnlyComposable
actual inline fun HeadContext.IncludeScript(async: Boolean, defer: Boolean) {}
