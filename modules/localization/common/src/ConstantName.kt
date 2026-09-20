package net.derfruhling.serenity.localization

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * A hashed string representing some name to be localized at runtime. The
 * constructor, despite being internal, shall be called by client code directly
 * using the Serenity compiler plugin. This is possible by virtue of it being
 * [@PublishedApi][PublishedApi], and by me being a psychopath.
 *
 * This class exists so the compiled application doesn't need to care about
 * the names, which should hopefully result in more efficient code with little
 * effort.
 *
 * Use like so:
 * ```kotlin
 * val name: ConstantName = n("name")
 * ```
 */
@JvmInline
@Serializable
value class ConstantName @PublishedApi internal constructor(val asLong: Long) {
    override fun toString(): String {
        return "Name(${asLong.toULong().toHexString(hexFormat)})"
    }

    inline val asULong: ULong
        get() = asLong.toULong()

    internal constructor(text: String) : this(XXH3.digest(text.encodeToByteArray()).toLong())

    companion object {
        private val hexFormat = HexFormat {
            number {
                minLength = 16
                prefix = "0x"
            }
        }
    }
}

/**
 * Constructs a [ConstantName] object from a name, at compile time if possible.
 * The actual implementation of this function uses Serenity's builtin XXH3
 * implementation at runtime, allowing use of string interpolation and the like.
 *
 * The Serenity compiler plugin should be installed to optimize away calls to
 * this function.
 *
 * @param name The name to hash.
 * @return A 64-bit XXH3 hash of the provided name.
 */
fun n(@Suppress("unused") name: String): ConstantName {
    return ConstantName(name)
}
