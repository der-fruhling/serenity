package net.derfruhling.serenity.embeds.opengraph

import net.derfruhling.serenity.embeds.MutableEmbedConfig

class WebsiteOpenGraphEmbed : AbstractOpenGraphEmbed<MutableOpenGraphEmbedConfig, OpenGraphEmbedConfig>() {
    override fun createConfig(base: MutableEmbedConfig): MutableOpenGraphEmbedConfig {
        return MutableOpenGraphEmbedConfig(base)
    }

    override val ogType: String
        get() = "website"
}

fun MutableEmbedConfig.ogWebsite(fn: MutableOpenGraphEmbedConfig.() -> Unit = {}) {
    installEmbed(::WebsiteOpenGraphEmbed, fn)
}
