package net.derfruhling.serenity.embeds.discord

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.derfruhling.serenity.elements.HeadContext
import net.derfruhling.serenity.embeds.AbstractConfiguredEmbed
import net.derfruhling.serenity.embeds.EmbedConfig
import net.derfruhling.serenity.embeds.MutableEmbedConfig
import net.derfruhling.serenity.serial.SerialRegistry

class DiscordEmbed : AbstractConfiguredEmbed<MutableDiscordConfig>() {
    private lateinit var _config: MutableDiscordConfig
    private val config by lazy { DiscordConfig(_config) }

    override fun configure(
        base: MutableEmbedConfig,
        cfg: MutableDiscordConfig.() -> Unit
    ) {
        _config = MutableDiscordConfig().apply(cfg)
    }

    val componentJson by lazy { Json.encodeToString(config) }

    @Composable
    override fun HeadContext.content(embedConfig: EmbedConfig) {
        inlineScriptJsonTag(
            "application/json",
            id = "discord:component-embed",
            content = componentJson
        )
    }
}

fun MutableEmbedConfig.discord(cfg: MutableDiscordConfig.() -> Unit) =
    installEmbed(::DiscordEmbed, cfg)

inline fun MutableEmbedConfig.discordComponent(
    accentColor: Int? = null,
    spoiler: Boolean = false,
    crossinline cfg: ComponentBuilder.() -> Unit
) = discord {
    component = container(accentColor, spoiler) { cfg() }
}

private val defaultComponent by lazy { container { } }

class MutableDiscordConfig {
    var component: DiscordComponent = defaultComponent
}

@Serializable
class DiscordConfig private constructor(val component: DiscordComponent) {
    internal constructor(from: MutableDiscordConfig) : this(from.component)
}
