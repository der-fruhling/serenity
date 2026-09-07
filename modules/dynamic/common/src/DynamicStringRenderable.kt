package net.derfruhling.serenity.dynamic

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.HtmlComposable

interface DynamicStringRenderable {
    @Composable
    @HtmlComposable
    fun Render(context: DynamicStringContext)
}
