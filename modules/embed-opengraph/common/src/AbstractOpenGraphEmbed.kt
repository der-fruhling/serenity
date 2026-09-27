package net.derfruhling.serenity.embeds.opengraph

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.elements.HeadContext
import net.derfruhling.serenity.embeds.AbstractPolymorphicConfiguredEmbed
import net.derfruhling.serenity.embeds.EmbedConfig
import net.derfruhling.serenity.embeds.MutableEmbedConfig
import net.derfruhling.serenity.embeds.maybe

abstract class AbstractOpenGraphEmbed<MutCfg : MutableOpenGraphEmbedConfig, Cfg : OpenGraphEmbedConfig> :
    AbstractPolymorphicConfiguredEmbed<AbstractOpenGraphEmbed<*, *>, MutCfg>(
        AbstractOpenGraphEmbed::class
    ) {
    private lateinit var _config: Cfg

    protected val config: Cfg by ::_config

    protected abstract fun createConfig(base: MutableEmbedConfig): MutCfg
    abstract val ogType: String

    override fun configure(base: MutableEmbedConfig, cfg: MutCfg.() -> Unit) {
        @Suppress("UNCHECKED_CAST")
        _config = createConfig(base).apply(cfg).freeze() as Cfg
    }

    private val imagesAvailable by lazy { config.images.isNotEmpty() }
    private val videosAvailable by lazy { config.videos.isNotEmpty() }
    private val audiosAvailable by lazy { config.audios.isNotEmpty() }

    @Composable
    override fun HeadContext.content(embedConfig: EmbedConfig) {
        meta("og:type", ogType)
        config.title?.let { meta("og:title", it) }
        config.url?.let { meta("og:url", it) }
        config.description?.let { meta("og:description", it) }
        config.determiner?.let { meta("og:determiner", it) }
        config.locale?.let { meta("og:locale", it) }
        config.alternateLocales?.let { array ->
            array.forEach {
                meta("og:locale:alternate", it)
            }
        }
        config.siteName?.let { meta("og:site_name", it) }

        if (imagesAvailable) {
            ogImages()
        }

        if (videosAvailable) {
            ogVideos()
        }

        if (audiosAvailable) {
            ogAudios()
        }
    }

    @Composable
    private fun HeadContext.ogImages() {
        for (image in config.images) {
            meta("og:image", image.url)
            image.width.maybe { meta("og:image:width", it.toString()) }
            image.height.maybe { meta("og:image:height", it.toString()) }
            image.type?.let { meta("og:image:type", it) }
            image.alt?.let { meta("og:image:alt", it) }

            image.with<OpenGraphImageExt> { ext ->
                ext.secureUrl?.let { meta("og:image:secure_url", it) }
            }
        }
    }

    @Composable
    private fun HeadContext.ogVideos() {
        for (video in config.videos) {
            meta("og:video", video.url)
            video.width.maybe { meta("og:video:width", it.toString()) }
            video.height.maybe { meta("og:video:height", it.toString()) }
            video.type?.let { meta("og:video:type", it) }
            video.alt?.let { meta("og:video:alt", it) }

            video.with<OpenGraphImageExt> { ext ->
                ext.secureUrl?.let { meta("og:video:secure_url", it) }
            }
        }
    }

    @Composable
    private fun HeadContext.ogAudios() {
        for (audio in config.audios) {
            meta("og:audio", audio.url)
            audio.type?.let { meta("og:audio:type", it) }
            audio.secureUrl?.let { meta("og:audio:secure_url", it) }
        }
    }
}

