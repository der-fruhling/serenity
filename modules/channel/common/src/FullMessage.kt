package net.derfruhling.serenity.channel

import kotlinx.serialization.Serializable

@Serializable
data class FullMessage(val id: Int, val content: Message)
