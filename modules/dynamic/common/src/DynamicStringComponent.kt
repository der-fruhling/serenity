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
import kotlin.jvm.JvmInline

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

    object Serializer : KSerializer<DynamicStringComponent> {
        const val SEQUENCE: Int = 0
        const val STRING: Int = 1
        const val BOLD: Int = 2
        const val ITALIC: Int = 3
        const val ARGUMENT: Int = 4

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
                DynamicStringComponent.Bold.serializer().descriptor,
                listOf(CborLabel(BOLD.toLong())),
                isOptional = true
            )
            element(
                "asItalic",
                DynamicStringComponent.Italic.serializer().descriptor,
                listOf(CborLabel(ITALIC.toLong())),
                isOptional = true
            )
            element<Argument>("asArgument", listOf(CborLabel(ARGUMENT.toLong())), isOptional = true)
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

                is DynamicStringComponent.Bold -> encodeSerializableElement(descriptor, BOLD, DynamicStringComponent.Bold.serializer(), value)
                is DynamicStringComponent.Italic -> encodeSerializableElement(descriptor, ITALIC, DynamicStringComponent.Italic.serializer(), value)
                is Sequence -> encodeSerializableElement(
                    descriptor,
                    SEQUENCE,
                    Sequence.serializer(),
                    value
                )

                is StringConst -> encodeStringElement(descriptor, STRING, value.text)
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
                    else -> throw IllegalArgumentException("Unknown element $index")
                }
            }
    }
}

fun String.toComponent(): DynamicStringComponent = DynamicStringComponent.StringConst(this)
