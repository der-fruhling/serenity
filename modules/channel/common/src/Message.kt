package net.derfruhling.serenity.channel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed class Message {
    interface S2C
    interface C2S

    interface Bidirectional : S2C, C2S

    abstract val isCancellable: Boolean

    @Serializable
    @SerialName($$"$ch:Disconnect")
    data object Disconnect : Message(), Bidirectional {
        @Transient
        override val isCancellable: Boolean
            get() = false
    }

    @Serializable
    @SerialName($$"$ch:Error")
    data class Error(
        val message: String,
        val messageId: Int? = null
    ) : Message(), Bidirectional {
        override val isCancellable: Boolean
            get() = false
    }

    @Serializable
    @SerialName($$"$ch:Hello")
    data class Hello(val sessionId: Int) : Message(), S2C {
        override val isCancellable: Boolean
            get() = false
    }

    @Serializable
    @SerialName($$"$ch:Refresh")
    data object Refresh : Message(), S2C {
        override val isCancellable: Boolean
            get() = true
    }

    @Serializable
    @SerialName($$"$ch:InvalidateKey")
    data class InvalidateKey(val keys: List<String>) : Message(), S2C {
        constructor(vararg keys: String) : this(keys.toList())
        constructor(key: String) : this(listOf(key))

        override val isCancellable: Boolean
            get() = true
    }
}
