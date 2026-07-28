package net.derfruhling.serenity

import net.derfruhling.serenity.annotations.HashCodeDsl
import kotlin.jvm.JvmName

@HashCodeDsl
class HashCodeBuilder {
    var value: Int = 0

    @Suppress("NOTHING_TO_INLINE")
    inline fun <T> with(value: T) {
        this.value = 31 * this.value + value.hashCode()
    }

    @Suppress("NOTHING_TO_INLINE")
    @JvmName("withNullable")
    inline fun with(value: Any?) {
        this.value = 31 * this.value + value.hashCode()
    }
}

inline fun <reified T> T.hash(fn: HashCodeBuilder.() -> Unit): Int {
    return HashCodeBuilder().apply {
        with(T::class)
        fn()
    }.value
}
