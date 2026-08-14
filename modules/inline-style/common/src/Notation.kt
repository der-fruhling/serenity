package net.derfruhling.serenity.style

import androidx.compose.runtime.Stable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun optRequire(condition: Boolean, message: () -> String) {
    contract {
        returns() implies condition
    }

    if(!condition) throw UnparsableNotationException(message())
}

@Stable
interface Notation<T> {
    fun test(value: Any): Boolean

    fun asNotationString(value: T): String

    @Throws(UnparsableNotationException::class, UnsupportedOperationException::class)
    fun fromNotationString(value: String): T

    companion object {
        inline fun <reified T : Any> of(vararg notations: Notation<out T>): Notation<T> {
            return object : Notation<T> {
                override fun test(value: Any): Boolean {
                    return value is T
                }

                override fun asNotationString(value: T): String {
                    for (n in notations) {
                        if (n.test(value)) {
                            @Suppress("UNCHECKED_CAST")
                            return (n as Notation<in T>).asNotationString(value)
                        }
                    }

                    throw IllegalStateException("Could not emit aggregate type ${T::class.simpleName!!} as $value is of an unknown type")
                }

                override fun fromNotationString(value: String): T {
                    val suppressed = mutableListOf<UnparsableNotationException>()

                    for(n in notations) {
                        try {
                            return n.fromNotationString(value)
                        } catch (e: UnparsableNotationException) {
                            suppressed.add(e)
                        }
                    }

                    throw UnparsableNotationException("Could not parse aggregate type ${T::class}").also {
                        for(s in suppressed) {
                            it.addSuppressed(s)
                        }
                    }
                }
            }
        }
    }
}
