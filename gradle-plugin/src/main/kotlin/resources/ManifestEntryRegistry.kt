package net.derfruhling.html.gradle.resources

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import org.gradle.internal.extensions.stdlib.uncheckedCast
import kotlin.reflect.KClass

object ManifestEntryRegistry {
    private data class Entry<T : ManifestEntry>(val kClass: KClass<T>, val kSerializer: KSerializer<T>)

    private val entries = mutableListOf<Entry<*>>()

    internal val serializersModule by lazy {
        SerializersModule {
            polymorphic(ManifestEntry::class) {
                for((kClass, kSerializer) in entries) {
                    subclass(kClass.uncheckedCast<KClass<ManifestEntry>>(), kSerializer.uncheckedCast())
                }
            }
        }
    }

    fun <T : ManifestEntry> registerEntry(kClass: KClass<T>, kSerializer: KSerializer<T>) {
        entries.add(Entry(kClass, kSerializer))
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    @JvmOverloads
    fun <T : ManifestEntry> registerEntry(clazz: Class<T>, kSerializer: KSerializer<T> = serializer(clazz.kotlin, emptyList(), false) as KSerializer<T>) {
        registerEntry(clazz.kotlin, kSerializer)
    }

    inline fun <reified T : ManifestEntry> registerEntry() {
        registerEntry(T::class, serializer())
    }

    init {
        registerEntry<ResourceIndexBuilder>()
    }
}