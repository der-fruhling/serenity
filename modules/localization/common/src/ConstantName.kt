package net.derfruhling.serenity.localization

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class ConstantName @PublishedApi internal constructor(val asLong: Long) {
    override fun toString(): String {
        return "Name(${asLong.toULong().toHexString(hexFormat)})"
    }

    internal constructor(text: String) : this(XXH3.digest(text.encodeToByteArray()))

    companion object {
        private val hexFormat = HexFormat {
            number {
                minLength = 16
                prefix = "0x"
            }
        }
    }
}

fun n(@Suppress("unused") name: String): ConstantName {
    return ConstantName(name)
}
