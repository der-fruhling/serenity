package net.derfruhling.serenity.manifest

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.cast

@Serializable(with = Manifest.Serializer::class)
class Manifest(
    private val entries: MutableMap<KClass<out ManifestEntry>, ManifestEntry>
) {
    object Serializer : KSerializer<Manifest> {
        private val listSerializer = serializer<List<ManifestEntry>>()

        override val descriptor: SerialDescriptor = SerialDescriptor($$"$manifest", listSerializer.descriptor)

        override fun serialize(
            encoder: Encoder,
            value: Manifest
        ) {
            encoder.encodeSerializableValue(
                listSerializer,
                value.entries.values.toList().filterIsInstance<SharedManifestEntry>()
            )
        }

        override fun deserialize(decoder: Decoder): Manifest {
            val entries = decoder.decodeSerializableValue(listSerializer)
                .associateBy { it::class }
                .toMutableMap()
            return Manifest(entries)
        }
    }

    operator fun <T : ManifestEntry> get(kClass: KClass<T>): T? {
        return entries[kClass]?.let { kClass.cast(it) }
    }

    operator fun <T : ManifestEntry> set(kClass: KClass<T>, value: T) {
        entries[kClass] = value
    }

    inline operator fun <reified T : ManifestEntry> getValue(self: Any?, property: KProperty<*>): T? {
        return this[T::class]
    }

    inner class Initializing<T : ManifestEntry> @PublishedApi internal constructor(
        val kClass: KClass<T>,
        val initializer: () -> T
    ) {
        private val value by lazy {
            this@Manifest[kClass] ?: initializer().also { this@Manifest[kClass] = it }
        }

        operator fun getValue(self: Any?, property: KProperty<*>): T = value
    }

    inline fun <reified T : ManifestEntry> initializing(noinline initializer: () -> T): Initializing<T> {
        return Initializing(T::class, initializer)
    }
}
