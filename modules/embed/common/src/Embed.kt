package net.derfruhling.serenity.embeds

import net.derfruhling.serenity.PageContract
import net.derfruhling.serenity.elements.HeadContext
import net.derfruhling.serenity.extend
import net.derfruhling.serenity.modularity.extension.AbstractPageExtension
import net.derfruhling.serenity.modularity.extension.PageExtensionPoints

class Embed : AbstractPageExtension<MutableEmbedConfig>() {
    private lateinit var _config: MutableEmbedConfig
    private val config by lazy { EmbedConfig(_config) }

    override fun configure(page: PageContract, fn: MutableEmbedConfig.() -> Unit) {
        _config = MutableEmbedConfig(page).apply(fn)
    }

    override fun initialize(extends: PageExtensionPoints) {
        extends.head {
            with(HeadContext) {
                for(embed in config.embeds) {
                    with(embed) { content(config) }
                }
            }
        }
    }
}

fun PageContract.embed(fn: MutableEmbedConfig.() -> Unit) = extend(::Embed, fn)
