package net.derfruhling.serenity

import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

object SerialRegistry {
    private data class SerialEntry<T : Any>(val kClass: KClass<T>, val kSerializer: KSerializer<T>)

    private val composedModules = mutableListOf<SerializersModule>()
    private val subclasses = mutableMapOf<KClass<*>, SerialEntry<*>>()
    private val pages = mutableMapOf<KClass<out SerialPageHolder>, SerialEntry<out SerialPageHolder>>()

    private var _serializersModule: SerializersModule? = null
    private var _json: Json? = null

    private inline fun <reified T> wrap(fn: () -> KSerializer<T> = { serializer<T>() }): KSerializer<T> {
        return Wrapped.Serializer(fn())
    }

    private inline fun <reified T> wrap(
        name: String,
        fn: () -> KSerializer<T> = { serializer<T>() }
    ): KSerializer<T> {
        return Wrapped.Serializer(fn(), name)
    }

    private fun buildSerializersModule() = SerializersModule {
        polymorphic(SerialPageHolder::class) {
            @Suppress("DestructuringDeclaration")
            for (page in pages.values) {
                subclass(page.kClass, page.kSerializer)
            }
        }

        polymorphic(Any::class) {
            subclass(SerialSavedData::class)
            subclass(SerialSavedState::class)
            subclass(MutableStateWrapper.serializer(PolymorphicSerializer(Any::class)))
            subclass(Boolean::class, wrap($$"$bool"))
            subclass(Char::class, wrap($$"$char"))
            subclass(String::class, wrap($$"$string"))
            subclass(Double::class, wrap($$"$double"))
            subclass(Float::class, wrap($$"$float"))
            subclass(Int::class, wrap($$"$int"))
            subclass(Long::class, wrap($$"$long"))
            subclass(BooleanArray::class, wrap($$"$bool[]"))
            subclass(CharArray::class, wrap($$"$char[]"))
            subclass(DoubleArray::class, wrap($$"$double[]"))
            subclass(FloatArray::class, wrap($$"$float[]"))
            subclass(IntArray::class, wrap($$"$int[]"))
            subclass(LongArray::class, wrap($$"$long[]"))

            @Suppress("DestructuringDeclaration")
            for (subClass in subclasses.values) {
                subclass(subClass.kClass, subClass.kSerializer)
            }
        }

        composedModules.forEach { include(it) }
    }

    val serializersModule: SerializersModule
        get() {
            return _serializersModule ?: buildSerializersModule().also { _serializersModule = it }
        }

    @Suppress("JSON_FORMAT_REDUNDANT")
    val json: Json
        get() {
            return _json ?: Json {
                this.serializersModule = this@SerialRegistry.serializersModule
            }.also { _json = it }
        }

    fun <T : Any> register(kClass: KClass<T>, kSerializer: KSerializer<T>) {
        subclasses[kClass] = SerialEntry(kClass, kSerializer)
        _json = null
        _serializersModule = null
    }

    fun <T : PageHolder<*>> registerPage(kClass: KClass<T>, kSerializer: KSerializer<T>) {
        pages[kClass] = SerialEntry(kClass, kSerializer)
        _json = null
        _serializersModule = null
    }

    inline fun <reified T : Any> register() {
        register(T::class, serializer<T>())
    }

    inline fun <reified T> decode(value: String): T {
        return json.decodeFromString(value)
    }

    inline fun <reified T> encode(value: T): String {
        return json.encodeToString(value)
    }
}
