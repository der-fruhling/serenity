package net.derfruhling.serenity.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Updater
import net.derfruhling.serenity.Element
import net.derfruhling.serenity.platform.ElementNode

object HtmlContext {
    @Composable
    fun head(content: @Composable HeadContext.() -> Unit) =
        Element(name = "head") { HeadContext.content() }

    @Composable
    fun body(updateBody: Updater<ElementNode>.() -> Unit = {}, content: @Composable () -> Unit) =
        Element(name = "body", updateBody, { content() })
}