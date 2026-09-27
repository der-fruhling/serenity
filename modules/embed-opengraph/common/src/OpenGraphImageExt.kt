package net.derfruhling.serenity.embeds.opengraph

import androidx.compose.runtime.Immutable
import net.derfruhling.serenity.embeds.EmbedsDsl
import net.derfruhling.serenity.embeds.Freezable
import net.derfruhling.serenity.embeds.MutableEmbedImage

@EmbedsDsl
class MutableOpenGraphImageExt : Freezable {
    var secureUrl: String? = null

    override fun freeze(): Any {
        return OpenGraphImageExt(this)
    }
}

@Immutable
class OpenGraphImageExt(from: MutableOpenGraphImageExt) {
    val secureUrl: String? = from.secureUrl
}

inline fun MutableEmbedImage.openGraph(fn: MutableOpenGraphImageExt.() -> Unit) =
    install(::MutableOpenGraphImageExt).fn()
