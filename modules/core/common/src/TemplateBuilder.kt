package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import net.derfruhling.serenity.elements.HeadContext

@Stable
interface TemplateBuilder {
    @Composable
    @HtmlComposable
    fun HeadContext.SlotHead()

    @Composable
    @HtmlComposable
    fun WithPage(fn: @Composable @HtmlComposable (PageHolder<*>) -> Unit)

    @Composable
    @HtmlComposable
    fun SlotBody()
}
