package net.derfruhling.serenity.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import net.derfruhling.serenity.annotations.HtmlComposable

@Suppress("NOTHING_TO_INLINE")
@Composable
@HtmlComposable
@ReadOnlyComposable
actual inline fun HeadContext.IncludeScript(async: Boolean, defer: Boolean) {}
