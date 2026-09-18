@file:OptIn(ExperimentalSerializationApi::class)

package net.derfruhling.serenity.dynamic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import net.derfruhling.serenity.Text
import net.derfruhling.serenity.elements.Bold
import net.derfruhling.serenity.elements.Italic
import net.derfruhling.serenity.elements.Strikethrough
import net.derfruhling.serenity.elements.Underline
import kotlin.jvm.JvmInline
import net.derfruhling.serenity.elements.Link as RealLink
import net.derfruhling.serenity.elements.Paragraph as RealParagraph
import net.derfruhling.serenity.elements.Span as RealSpan

@Serializable(with = DynamicStringComponent.Serializer::class)
@Immutable
sealed interface DynamicStringComponent : DynamicStringRenderable {
    @Serializable
    @SerialName($$"$d-string")
    @Immutable
    @JvmInline
    value class StringConst(val text: String) : DynamicStringComponent {
        @Composable
        override fun Render(context: DynamicStringContext) {
            Text(text)
        }
    }

    @Serializable
    @SerialName($$"$d-bold")
    @Immutable
    @JvmInline
    value class Bold(val content: DynamicStringComponent) : DynamicStringComponent {
        @Composable
        override fun Render(context: DynamicStringContext) {
            Bold {
                content.Render(context)
            }
        }
    }

    @Serializable
    @SerialName($$"$d-italic")
    @Immutable
    @JvmInline
    value class Italic(val content: DynamicStringComponent) : DynamicStringComponent {
        @Composable
        override fun Render(context: DynamicStringContext) {
            Italic {
                content.Render(context)
            }
        }
    }

    @Serializable
    @SerialName($$"$d-seq")
    @Immutable
    @JvmInline
    value class Sequence(val children: List<DynamicStringComponent>) : DynamicStringComponent {
        @Composable
        override fun Render(context: DynamicStringContext) {
            for(child in children) {
                child.Render(context)
            }
        }
    }

    @Serializable
    @SerialName($$"$d-arg")
    @Immutable
    @CborArray
    data class Argument(val index: Int, val fallback: DynamicStringComponent? = null) : DynamicStringComponent {
        @Composable
        override fun Render(context: DynamicStringContext) {
            context.args.elementAtOrNull(index)?.Render(context)
                ?: fallback?.Render(context)
        }
    }

    @Serializable
    @SerialName($$"$d-link")
    @CborArray
    data class Link(val href: String, val content: DynamicStringComponent) :
        DynamicStringComponent {
        @Composable
        override fun Render(context: DynamicStringContext) {
            RealLink(href) {
                content.Render(context)
            }
        }
    }

    @Serializable
    @SerialName($$"$d-span")
    @CborArray
    data class Span(val title: String? = null, val content: DynamicStringComponent) :
        DynamicStringComponent {
        @Composable
        override fun Render(context: DynamicStringContext) {
            RealSpan(title) { 
                content.Render(context)
            }
        }
    }

    @Serializable
    @SerialName($$"$d-para")
    @JvmInline
    value class Paragraph(val content: DynamicStringComponent) : DynamicStringComponent {
        @Composable
        override fun Render(context: DynamicStringContext) {
            RealParagraph { 
                content.Render(context)
            }
        }
    }

    @Serializable
    @SerialName($$"$d-uline")
    @JvmInline
    value class Underlined(val content: DynamicStringComponent) : DynamicStringComponent {
        @Composable
        override fun Render(context: DynamicStringContext) {
            Underline {
                content.Render(context)
            }
        }
    }

    @Serializable
    @SerialName($$"$d-strike")
    @JvmInline
    value class Strike(val content: DynamicStringComponent) : DynamicStringComponent {
        @Composable
        override fun Render(context: DynamicStringContext) {
            Strikethrough {
                content.Render(context)
            }
        }
    }

    object Serializer : KSerializer<DynamicStringComponent> {
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
            element<Link>("asLink", listOf(CborLabel(
                LINK.toLong())), isOptional = true)
            element<Span>("asSpan", listOf(CborLabel(
                SPAN.toLong())), isOptional = true)
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
            value: DynamicStringComponent
        ) = encoder.encodeStructure(descriptor) {
            when (value) {
                is Argument -> encodeSerializableElement(
                    descriptor,
                    ARGUMENT,
                    Argument.serializer(),
                    value
                )

                is Bold -> encodeSerializableElement(descriptor, BOLD, Bold.serializer(), value)
                is Italic -> encodeSerializableElement(descriptor, ITALIC, Italic.serializer(), value)
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

        override fun deserialize(decoder: Decoder): DynamicStringComponent =
            decoder.decodeStructure(descriptor) {
                when (val index = decodeElementIndex(descriptor)) {
                    SEQUENCE -> decodeSerializableElement(descriptor, index, Sequence.serializer())
                    STRING -> StringConst(decodeStringElement(descriptor, index))
                    BOLD -> decodeSerializableElement(descriptor, index, DynamicStringComponent.Bold.serializer())
                    ITALIC -> decodeSerializableElement(descriptor, index, DynamicStringComponent.Italic.serializer())
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

fun String.toComponent(): DynamicStringComponent = DynamicStringComponent.StringConst(this)
