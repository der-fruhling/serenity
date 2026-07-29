package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Polymorphic
import net.derfruhling.serenity.annotations.HtmlComposable

@Immutable
@Polymorphic
interface PageHolder {
    val id: String
    val path: String

    @Composable
    @HtmlComposable
    fun Main()
}

