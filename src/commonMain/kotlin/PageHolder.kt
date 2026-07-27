package net.derfruhling.html

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import net.derfruhling.html.annotations.HtmlComposable

@Immutable
interface PageHolder {
    val id: String
    val path: String

    @Composable
    @HtmlComposable
    fun Main()
}
