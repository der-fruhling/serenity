package net.derfruhling.serenity.channel.ktor

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.websocket.*
import net.derfruhling.serenity.channel.FullMessage
import net.derfruhling.serenity.serial.SerialRegistry

open class JsonFrameTransport : FrameTransport {
    private val logger = KotlinLogging.logger {}

    override fun encodeMessage(message: FullMessage): Frame {
        val json = SerialRegistry.encode(message)
        return Frame.Text(json)
    }

    override fun decodeMessage(frame: Frame): FullMessage? {
        return when (frame) {
            is Frame.Text -> SerialRegistry.decode<FullMessage>(frame.readText())

            else -> {
                logger.warn { "Wrong frame of type ${frame::class.simpleName!!} received" }
                null
            }
        }
    }

    companion object : JsonFrameTransport()
}
