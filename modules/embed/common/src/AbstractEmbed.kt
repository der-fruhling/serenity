package net.derfruhling.serenity.embeds

import androidx.compose.runtime.Composable
import net.derfruhling.serenity.HtmlComposable
import net.derfruhling.serenity.elements.HeadContext
import kotlin.reflect.KClass

sealed class AbstractEmbed {
    @Composable
    @HtmlComposable
    abstract fun HeadContext.content(embedConfig: EmbedConfig)

    open val typeKey: KClass<out AbstractEmbed>
        get() = this::class
}

abstract class AbstractEmbedVoid : AbstractEmbed()
abstract class AbstractConfiguredEmbed<Cfg> : AbstractEmbed() {
    abstract fun configure(base: MutableEmbedConfig, cfg: Cfg.() -> Unit)
}

abstract class AbstractPolymorphicEmbedVoid<out T : AbstractEmbed>(override val typeKey: KClass<out T>) : AbstractEmbedVoid()
abstract class AbstractPolymorphicConfiguredEmbed<out T : AbstractEmbed, Cfg>(override val typeKey: KClass<out T>) : AbstractConfiguredEmbed<Cfg>()
