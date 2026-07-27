package net.derfruhling.html

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Wrapped<T>(val value: T) {
    class Serializer<T>(val type: KSerializer<T>, val name: String = type.descriptor.serialName) : KSerializer<T> {
        val serializer = serializer(type)
        override val descriptor: SerialDescriptor = SerialDescriptor(name, serializer.descriptor)

        override fun serialize(encoder: Encoder, value: T) {
            serializer.serialize(encoder, Wrapped(value))
        }

        override fun deserialize(decoder: Decoder): T {
            return serializer.deserialize(decoder).value
        }
    }
}