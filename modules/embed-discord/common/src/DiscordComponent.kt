package net.derfruhling.serenity.embeds.discord

import kotlinx.serialization.*
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@Serializable(with = DiscordComponent.Serializer::class)
sealed class DiscordComponent {
    abstract val type: Int

    object Serializer : KSerializer<DiscordComponent> {
        @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor = buildSerialDescriptor(
            "net.derfruhling.serenity.embeds.discord.DiscordComponent",
            PolymorphicKind.SEALED
        ) {
            element("type", Int.serializer().descriptor)
            element(
                "value",
                buildSerialDescriptor(
                    "kotlinx.serialization.Sealed<DiscordComponent>",
                    SerialKind.CONTEXTUAL
                )
            )
        }

        override fun serialize(
            encoder: Encoder,
            value: DiscordComponent
        ) {
            (encoder as JsonEncoder).encodeJsonElement(buildJsonObject {
                val json = encoder.json
                put("type", JsonPrimitive(value.type))
                val tree = when(value) {
                    is ActionRow -> json.encodeToJsonElement(ActionRow.serializer(), value)
                    is Button -> json.encodeToJsonElement(Button.serializer(), value)
                    is Container -> json.encodeToJsonElement(Container.serializer(), value)
                    is MediaGallery -> json.encodeToJsonElement(MediaGallery.serializer(), value)
                    is Section -> json.encodeToJsonElement(Section.serializer(), value)
                    is Separator -> json.encodeToJsonElement(Separator.serializer(), value)
                    is TextDisplay -> json.encodeToJsonElement(TextDisplay.serializer(), value)
                    is Thumbnail -> json.encodeToJsonElement(Thumbnail.serializer(), value)
                } as JsonObject

                for((key, value) in tree) {
                    put(key, value)
                }
            })
        }

        override fun deserialize(decoder: Decoder): DiscordComponent {
            throw NotImplementedError()
        }
    }
}

@Serializable
data class ActionRow(
    val id: Int = -1,
    val components: List<DiscordComponent>
) : DiscordComponent() {
    override val type: Int
        get() = 1
}

@Serializable(with = UnsafeButtonStyle.Serializer::class)
internal enum class UnsafeButtonStyle(val number: Int) {
    LINK(5);

    object Serializer : KSerializer<UnsafeButtonStyle> {
        override val descriptor: SerialDescriptor = SerialDescriptor(
            "net.derfruhling.serenity.embeds.discord.UnsafeButtonStyle",
            Int.serializer().descriptor
        )

        override fun serialize(
            encoder: Encoder,
            value: UnsafeButtonStyle
        ) {
            encoder.encodeInline(descriptor).encodeInt(value.number)
        }

        override fun deserialize(decoder: Decoder): UnsafeButtonStyle {
            val int = decoder.decodeInline(descriptor).decodeInt()
            require(int == 5) { "Not a link" }
            return LINK
        }

    }
}

@Serializable
data class Emoji(val id: String, val name: String, val animated: Boolean)

@Serializable
@ConsistentCopyVisibility
data class Button private constructor(
    val id: Int = -1,
    private val style: UnsafeButtonStyle,
    val label: String? = null,
    val emoji: Emoji? = null,
    @SerialName("custom_id")
    val customId: String? = null,
    val url: String? = null,
    val disabled: Boolean = false,
) : DiscordComponent() {
    override val type: Int
        get() = 2

    companion object {
        fun link(
            url: String,
            label: String? = null,
            emoji: Emoji? = null,
            disabled: Boolean = false,
            id: Int = -1
        ): Button = Button(id, UnsafeButtonStyle.LINK, label, emoji, url = url, disabled = disabled)
    }
}

@Serializable
data class Section(
    val id: Int = -1,
    val components: List<DiscordComponent>,
    val accessory: DiscordComponent? = null
) : DiscordComponent() {
    override val type: Int
        get() = 9
}

@Serializable
data class TextDisplay(
    val id: Int = -1,
    val content: String
) : DiscordComponent() {
    override val type: Int
        get() = 10
}

@Serializable
data class MediaItem(val url: String)

@Serializable
data class Thumbnail(
    val id: Int = -1,
    val media: MediaItem,
    val description: String? = null,
    val spoiler: Boolean = false,
) : DiscordComponent() {
    override val type: Int
        get() = 11
}

@Serializable
data class MediaGalleryItem(
    val media: MediaItem,
    val description: String? = null,
    val spoiler: Boolean = false
)

@Serializable
data class MediaGallery(
    val id: Int = -1,
    val items: List<MediaGalleryItem>
) : DiscordComponent() {
    override val type: Int
        get() = 12
}

@Serializable
data class Separator(
    val id: Int = -1,
    val divider: Boolean = true,
    val spacing: Int = 1,
) : DiscordComponent() {
    override val type: Int
        get() = 14


}

@Serializable
data class Container(
    val id: Int = -1,
    val components: List<DiscordComponent>,
    @SerialName("accent_color")
    val accentColor: Int? = null,
    val spoiler: Boolean = false,
) : DiscordComponent() {
    override val type: Int
        get() = 17
}
