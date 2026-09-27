package net.derfruhling.serenity.embeds.discord

import net.derfruhling.serenity.embeds.EmbedsDsl
import kotlin.jvm.JvmName

@EmbedsDsl
abstract class ComponentBuilder {
    abstract fun addChild(component: DiscordComponent)

    inline fun actionRow(fn: ComponentBuilder.() -> Unit) {
        addChild(ActionRowBuilder().apply(fn).build())
    }

    fun button(
        url: String,
        label: String? = null,
        emoji: Emoji? = null,
        disabled: Boolean = false
    ) {
        addChild(Button.link(url, label, emoji, disabled))
    }

    inline fun section(fn: SectionBuilder.() -> Unit) {
        addChild(SectionBuilderImpl().apply(fn).build())
    }

    fun text(string: String) {
        addChild(TextDisplay(content = string))
    }

    fun thumbnail(mediaUrl: String, description: String? = null, spoiler: Boolean = false) {
        addChild(
            Thumbnail(
                media = MediaItem(mediaUrl),
                description = description,
                spoiler = spoiler
            )
        )
    }

    inline fun mediaGallery(fn: MediaGalleryBuilder.() -> Unit) {
        addChild(MediaGalleryBuilderImpl().apply(fn).build())
    }

    fun separator(divider: Boolean = true, spacing: Int = 1) {
        addChild(Separator(divider = divider, spacing = spacing))
    }

    inline fun container(accentColor: Int? = null, spoiler: Boolean = false, fn: ComponentBuilder.() -> Unit) {
        addChild(ContainerBuilder(accentColor, spoiler).apply(fn).build())
    }
}

@PublishedApi
internal class OnceComponentBuilder : ComponentBuilder() {
    private lateinit var _value: DiscordComponent

    @PublishedApi
    internal val value: DiscordComponent?
        get() = if (::_value.isInitialized) _value else null

    @PublishedApi
    internal val valueRequired: DiscordComponent by ::_value

    override fun addChild(component: DiscordComponent) {
        if (::_value.isInitialized) throw IllegalStateException("Cannot initialize this component twice")
        _value = component
    }
}

@PublishedApi
internal class ActionRowBuilder : ComponentBuilder() {
    private val components: MutableList<DiscordComponent> = mutableListOf()

    override fun addChild(component: DiscordComponent) {
        components += component
    }

    fun build(): ActionRow {
        return ActionRow(components = components.toList())
    }
}

@EmbedsDsl
sealed class SectionBuilder : ComponentBuilder() {
    protected val components: MutableList<DiscordComponent> = mutableListOf()
    protected var accessory: DiscordComponent? = null

    override fun addChild(component: DiscordComponent) {
        components += component
    }

    @PublishedApi
    @JvmName("setAccessory$")
    internal fun setAccessory(component: DiscordComponent) {
        if (accessory != null) throw IllegalStateException("Accessory already initialized")
        accessory = component
    }

    inline fun accessory(fn: ComponentBuilder.() -> Unit) {
        OnceComponentBuilder().apply(fn).value?.let { setAccessory(it) }
    }
}

@PublishedApi
internal class SectionBuilderImpl : SectionBuilder() {
    fun build(): Section = Section(components = components, accessory = accessory)
}

@EmbedsDsl
sealed class MediaGalleryBuilder {
    protected val items = mutableListOf<MediaGalleryItem>()

    fun item(url: String, description: String? = null, spoiler: Boolean = false) {
        items += MediaGalleryItem(MediaItem(url), description, spoiler)
    }
}

@PublishedApi
internal class MediaGalleryBuilderImpl : MediaGalleryBuilder() {
    fun build(): MediaGallery {
        return MediaGallery(items = items)
    }
}

@PublishedApi
internal class ContainerBuilder(val accentColor: Int?, val spoiler: Boolean) : ComponentBuilder() {
    private val components: MutableList<DiscordComponent> = mutableListOf()

    override fun addChild(component: DiscordComponent) {
        components += component
    }

    fun build(): Container {
        return Container(components = components.toList(), accentColor = accentColor, spoiler = spoiler)
    }
}

inline fun container(accentColor: Int? = null, spoiler: Boolean = false, fn: ComponentBuilder.() -> Unit): DiscordComponent {
    return ContainerBuilder(accentColor, spoiler).apply(fn).build()
}
