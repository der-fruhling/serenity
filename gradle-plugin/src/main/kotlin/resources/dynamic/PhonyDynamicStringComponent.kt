@file:OptIn(ExperimentalSerializationApi::class)

package net.derfruhling.serenity.gradle.resources.dynamic

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.CborArray
import kotlinx.serialization.cbor.CborLabel
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

@Serializable(with = PhonyDynamicStringComponent.Serializer::class)
sealed interface PhonyDynamicStringComponent {
    @Serializable
    @SerialName($$"$d-string")
    @JvmInline
    value class StringConst(val text: String) : PhonyDynamicStringComponent

    @Serializable
    @SerialName($$"$d-bold")
    @JvmInline
    value class Bold(val content: PhonyDynamicStringComponent) : PhonyDynamicStringComponent

    @Serializable
    @SerialName($$"$d-italic")
    @JvmInline
    value class Italic(val content: PhonyDynamicStringComponent) : PhonyDynamicStringComponent

    @Serializable
    @SerialName($$"$d-seq")
    @JvmInline
    value class Sequence(val children: List<PhonyDynamicStringComponent>) :
        PhonyDynamicStringComponent

    @Serializable
    @SerialName($$"$d-arg")
    @CborArray
    data class Argument(val index: Int, var fallback: PhonyDynamicStringComponent? = null) :
        PhonyDynamicStringComponent

    @Serializable
    @SerialName($$"$d-link")
    @CborArray
    data class Link(val href: String, var content: PhonyDynamicStringComponent? = null) :
        PhonyDynamicStringComponent

    @Serializable
    @SerialName($$"$d-span")
    @CborArray
    data class Span(val title: String? = null, var content: PhonyDynamicStringComponent? = null) :
        PhonyDynamicStringComponent

    @Serializable
    @SerialName($$"$d-para")
    @JvmInline
    value class Paragraph(val content: PhonyDynamicStringComponent) : PhonyDynamicStringComponent

    @Serializable
    @SerialName($$"$d-uline")
    @JvmInline
    value class Underlined(val content: PhonyDynamicStringComponent) : PhonyDynamicStringComponent

    @Serializable
    @SerialName($$"$d-strike")
    @JvmInline
    value class Strike(val content: PhonyDynamicStringComponent) : PhonyDynamicStringComponent

    object Serializer : KSerializer<PhonyDynamicStringComponent> {
        const val SEQUENCE: Int = 0
        const val STRING: Int = 1
        const val BOLD: Int = 2
        const val ITALIC: Int = 3
        const val ARGUMENT: Int = 4
        const val LINK: Int = 5
        const val SPAN: Int = 6
        const val PARAGRAPH: Int = 7
        const val UNDERLINED: Int = 8
        const val STRIKE: Int = 9

        override val descriptor: SerialDescriptor = buildClassSerialDescriptor($$"$dyn-cmp") {
            element(
                "asSequence",
                Sequence.serializer().descriptor,
                listOf(CborLabel(SEQUENCE.toLong())),
                isOptional = true
            )
            element<String>("asString", listOf(CborLabel(STRING.toLong())), isOptional = true)
            element(
                "asBold",
                Bold.serializer().descriptor,
                listOf(CborLabel(BOLD.toLong())),
                isOptional = true
            )
            element(
                "asItalic",
                Italic.serializer().descriptor,
                listOf(CborLabel(ITALIC.toLong())),
                isOptional = true
            )
            element<Argument>("asArgument", listOf(CborLabel(ARGUMENT.toLong())), isOptional = true)
            element<Link>("asLink", listOf(CborLabel(LINK.toLong())), isOptional = true)
            element<Span>("asSpan", listOf(CborLabel(SPAN.toLong())), isOptional = true)
            element(
                "asParagraph",
                Paragraph.serializer().descriptor,
                listOf(CborLabel(PARAGRAPH.toLong())),
                isOptional = true
            )
            element(
                "asUnderlined",
                Underlined.serializer().descriptor,
                listOf(CborLabel(UNDERLINED.toLong())),
                isOptional = true
            )
            element(
                "asStrike",
                Strike.serializer().descriptor,
                listOf(CborLabel(STRIKE.toLong())),
                isOptional = true
            )
        }

        override fun serialize(
            encoder: Encoder,
            value: PhonyDynamicStringComponent
        ) = encoder.encodeStructure(descriptor) {
            when (value) {
                is Argument -> encodeSerializableElement(
                    descriptor,
                    ARGUMENT,
                    Argument.serializer(),
                    value
                )

                is Bold -> encodeSerializableElement(descriptor, BOLD, Bold.serializer(), value)
                is Italic -> encodeSerializableElement(
                    descriptor,
                    ITALIC,
                    Italic.serializer(),
                    value
                )

                is Sequence -> encodeSerializableElement(
                    descriptor,
                    SEQUENCE,
                    Sequence.serializer(),
                    value
                )

                is StringConst -> encodeStringElement(descriptor, STRING, value.text)

                is Link -> encodeSerializableElement(descriptor, LINK, Link.serializer(), value)
                is Span -> encodeSerializableElement(descriptor, SPAN, Span.serializer(), value)
                is Paragraph -> encodeSerializableElement(descriptor, PARAGRAPH, Paragraph.serializer(), value)
                is Underlined -> encodeSerializableElement(descriptor, UNDERLINED, Underlined.serializer(), value)
                is Strike -> encodeSerializableElement(descriptor, STRIKE, Strike.serializer(), value)
            }
        }

        override fun deserialize(decoder: Decoder): PhonyDynamicStringComponent =
            decoder.decodeStructure(descriptor) {
                when (val index = decodeElementIndex(descriptor)) {
                    SEQUENCE -> decodeSerializableElement(descriptor, index, Sequence.serializer())
                    STRING -> StringConst(decodeStringElement(descriptor, index))
                    BOLD -> decodeSerializableElement(descriptor, index, Bold.serializer())
                    ITALIC -> decodeSerializableElement(descriptor, index, Italic.serializer())
                    ARGUMENT -> decodeSerializableElement(descriptor, index, Argument.serializer())
                    LINK -> decodeSerializableElement(descriptor, index, Link.serializer())
                    SPAN -> decodeSerializableElement(descriptor, index, Span.serializer())
                    PARAGRAPH -> decodeSerializableElement(descriptor, index, Paragraph.serializer())
                    UNDERLINED -> decodeSerializableElement(descriptor, index, Underlined.serializer())
                    STRIKE -> decodeSerializableElement(descriptor, index, Strike.serializer())
                    else -> throw IllegalArgumentException("Unknown element $index")
                }
            }
    }
}