package net.derfruhling.serenity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.derfruhling.serenity.annotations.HtmlComposable

@Immutable
@Polymorphic
interface PageHolder {
    val id: String
    val path: String

    val details: PageDetails

    @Composable
    @HtmlComposable
    fun Main()
}

@Serializable
@SerialName($$"$page-details")
data class PageDetails(val title: String? = null)
