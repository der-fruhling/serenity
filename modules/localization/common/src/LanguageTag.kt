package net.derfruhling.serenity.localization

import androidx.compose.runtime.Immutable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = LanguageTag.Serializer::class)
@Immutable
class LanguageTag(val string: String) {
    @Transient
    val languageString: String

    @Transient
    val language by lazy { Language.bySubtag[string] }

    @Transient
    val scriptString: String?

    @Transient
    val script by lazy {
        scriptString?.let {
            try {
                LangScript.valueOf(it)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }

    @Transient
    val areaString: String?

    @Transient
    val area by lazy {
        scriptString?.let {
            try {
                when (it.length) {
                    2 -> LangArea.fromISO3166(it)
                    3 -> LangArea.fromCode(it.toInt())
                    else -> null
                }
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }

    init {
        val parts = string.split('-').toMutableList()
        languageString = parts.first()

        var script: String? = null
        var area: String? = null

        val iterator = parts.listIterator(1)

        while (iterator.hasNext()) {
            val item = iterator.next()
            when (item.length) {
                1 -> {
                    /* TODO */
                }

                // area code
                2, 3 -> area = item.uppercase()

                // script
                4 -> script = item
            }
        }

        scriptString = script
        areaString = area
    }

    object Serializer : KSerializer<LanguageTag> {
        override val descriptor: SerialDescriptor = SerialDescriptor(
            "net.derfruhling.serenity.localization.LanguageTag",
            String.serializer().descriptor
        )

        override fun serialize(
            encoder: Encoder,
            value: LanguageTag
        ) {
            encoder.encodeInline(descriptor).encodeString(value.string)
        }

        override fun deserialize(decoder: Decoder): LanguageTag {
            return LanguageTag(decoder.decodeInline(descriptor).decodeString())
        }
    }

    companion object {
        val en = LanguageTag("en")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LanguageTag) return false

        if (string != other.string) return false

        return true
    }

    override fun hashCode(): Int {
        return string.hashCode()
    }

    override fun toString(): String {
        return string
    }
}