package net.derfruhling.serenity.embeds.opengraph

import androidx.compose.runtime.Immutable
import net.derfruhling.serenity.embeds.EmbedImage
import net.derfruhling.serenity.embeds.EmbedsDsl
import net.derfruhling.serenity.embeds.MutableEmbedConfig
import net.derfruhling.serenity.embeds.MutableEmbedImage

@EmbedsDsl
open class MutableOpenGraphEmbedConfig(private val base: MutableEmbedConfig) {
    var title: String? = null
        get() = field ?: base.title
    var url: String? = null
    var description: String? = null
        get() = field ?: base.description
    var determiner: String? = null
    var locale: String? = null
    var alternateLocales: Array<String>? = null
    var siteName: String? = null

    private val replacementImages: MutableList<EmbedImage> = mutableListOf()

    val images: List<EmbedImage>
        get() = (replacementImages as List<EmbedImage>).ifEmpty { base.images }

    @PublishedApi
    internal fun addImage(image: EmbedImage) {
        replacementImages += image
    }

    inline fun image(url: String, fn: MutableEmbedImage.() -> Unit) {
        addImage(EmbedImage(MutableEmbedImage(url).apply(fn)))
    }

    val videos: List<EmbedImage> field = mutableListOf()

    @PublishedApi
    internal fun addVideo(image: EmbedImage) {
        videos += image
    }

    inline fun video(url: String, fn: MutableEmbedImage.() -> Unit) {
        addVideo(EmbedImage(MutableEmbedImage(url).apply(fn)))
    }

    val audios: List<OpenGraphAudio> field = mutableListOf()

    @PublishedApi
    internal fun addAudio(image: OpenGraphAudio) {
        audios += image
    }

    inline fun audio(url: String, fn: MutableOpenGraphAudio.() -> Unit) {
        addAudio(OpenGraphAudio(MutableOpenGraphAudio(url).apply(fn)))
    }

    open fun freeze(): OpenGraphEmbedConfig = OpenGraphEmbedConfig(this)
}

@Immutable
open class OpenGraphEmbedConfig(from: MutableOpenGraphEmbedConfig) {
    val title: String? = from.title
    val url: String? = from.url
    val description: String? = from.description
    val determiner: String? = from.determiner
    val locale: String? = from.locale
    val alternateLocales: Array<String>? = from.alternateLocales
    val siteName: String? = from.siteName
    val images: List<EmbedImage> = from.images
    val videos: List<EmbedImage> = from.videos
    val audios: List<OpenGraphAudio> = from.audios
}
