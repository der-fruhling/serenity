package net.derfruhling.serenity.embeds.opengraph

import androidx.compose.runtime.Immutable
import net.derfruhling.serenity.embeds.EmbedsDsl

@EmbedsDsl
class MutableOpenGraphAudio(val url: String) {
    var secureUrl: String? = null
    var type: String? = null
}

@Immutable
class OpenGraphAudio(from: MutableOpenGraphAudio) {
    val url: String = from.url
    val secureUrl: String? = from.secureUrl
    val type: String? = from.type
}