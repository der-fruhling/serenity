package net.derfruhling.html

import androidx.compose.runtime.Composable
import net.derfruhling.html.annotations.HtmlComposable

interface PageHolder {
    val id: String
    val path: String

    @Composable
    @HtmlComposable
    fun Main()
}
