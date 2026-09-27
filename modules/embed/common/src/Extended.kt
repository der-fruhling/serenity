package net.derfruhling.serenity.embeds

import androidx.compose.runtime.Immutable
import kotlin.reflect.KClass

@EmbedsDsl
abstract class Extensible : MutableMap<KClass<*>, Any> by mutableMapOf() {
    inline fun <reified T : Any> install(fn: () -> T): T {
        return getOrPut(T::class, fn) as T
    }
}

interface Freezable {
    fun freeze(): Any
}

private fun Any.freeze(): Any {
    return when(this) {
        is Freezable -> freeze()
        else -> this
    }
}

@Immutable
abstract class Extended(extensible: Extensible) : Map<KClass<*>, Any> by (extensible.mapValues { (_, v) -> v.freeze() }) {
    inline fun <reified T : Any> with(fn: (T) -> Unit) {
        this[T::class]?.let { fn(it as T) }
    }
}
