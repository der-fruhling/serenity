package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import net.derfruhling.serenity.elements.HeadContext

@Stable
interface TemplateBuilder {
    @Composable
    fun HeadContext.SlotHead()

    @Composable
    fun WithPage(fn: @Composable (PageHolder<*>) -> Unit)

    @Composable
    fun SlotBody()
}
