package net.derfruhling.serenity.localization

import kotlin.jvm.JvmInline

@JvmInline
value class ConstantName private constructor(private val long: Long) {
    override fun toString(): String {
        return "Name(${long.toULong().toHexString(hexFormat)}"
    }

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
    throw UnsupportedOperationException("Cannot invoke this function from reflection")
}
