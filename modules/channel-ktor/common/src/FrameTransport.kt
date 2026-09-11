package net.derfruhling.serenity.channel.ktor

import io.ktor.websocket.Frame
import net.derfruhling.serenity.channel.FullMessage

interface FrameTransport {
    fun encodeMessage(message: FullMessage): Frame
    fun decodeMessage(frame: Frame): FullMessage?
}
