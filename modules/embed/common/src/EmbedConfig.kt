package net.derfruhling.serenity.embeds

import androidx.compose.runtime.Immutable
import net.derfruhling.serenity.PageContract
import kotlin.reflect.KClass

@EmbedsDsl
class MutableEmbedConfig(private val contract: PageContract) : Extensible() {
    var title: String? = null
        get() = field ?: contract.title
    var description: String? = null

    val images: List<EmbedImage> field = mutableListOf()

    internal val embeds = mutableMapOf<KClass<out AbstractEmbed>, AbstractEmbed>()

    fun <T : AbstractEmbedVoid> installEmbed(constructor: () -> T) {
        val embed = constructor()
        val typeKey = embed.typeKey

        if(typeKey in embeds) {
            throw IllegalStateException("Key $typeKey already is use for another embed")
        }

        embeds[typeKey] = embed
    }

    fun <T : AbstractConfiguredEmbed<Cfg>, Cfg> installEmbed(constructor: () -> T, cfg: Cfg.() -> Unit = {}) {
        val embed = constructor()

        // allow typeKey to depend on user configuration if needed
        embed.configure(this, cfg)
        val typeKey = embed.typeKey

        if(typeKey in embeds) {
            throw IllegalStateException("Key $typeKey already is use for another embed")
        }

        embeds[typeKey] = embed
    }

    @PublishedApi
    internal fun addImage(image: EmbedImage) {
        images += image
    }

    inline fun image(url: String, fn: MutableEmbedImage.() -> Unit) {
        addImage(EmbedImage(MutableEmbedImage(url).apply(fn)))
    }
}

@Immutable
class EmbedConfig internal constructor(from: MutableEmbedConfig) : Extended(from) {
    val title: String? = from.title
    val description: String? = from.description
    val images: List<EmbedImage> = from.images
    val embeds: Set<AbstractEmbed> = from.embeds.values.toSet()
}
