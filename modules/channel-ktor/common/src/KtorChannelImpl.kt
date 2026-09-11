package net.derfruhling.serenity.channel.ktor

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.websocket.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import kotlinx.serialization.SerializationException
import net.derfruhling.serenity.channel.AbstractConnection
import net.derfruhling.serenity.channel.ChannelExpectationsDiffer
import net.derfruhling.serenity.channel.ChannelImpl
import net.derfruhling.serenity.channel.FullMessage
import net.derfruhling.serenity.channel.Message

const val DEFAULT_ENDPOINT: String = "/_rc"

expect class KtorChannelConfig {
    var endpoint: String
    var maxFrameSize: Long?
    var pingIntervalMillis: Long?
    var frameTransport: FrameTransport
}

@OptIn(ChannelExpectationsDiffer::class)
expect class KtorChannelImpl() : ChannelImpl<KtorChannelConfig> {
    override fun configure(fn: KtorChannelConfig.() -> Unit)
}

internal class WebSocketConnectionImpl(
    id: Int,
    tx: Channel<FullMessage>,
    rx: Channel<FullMessage>,
    val frameTransport: FrameTransport
) : AbstractConnection(id, tx, rx) {
    private val logger = KotlinLogging.logger {}

    constructor(
        tx: Channel<FullMessage>,
        rx: Channel<FullMessage>,
        frameTransport: FrameTransport
    ) : this(0, tx, rx, frameTransport)

    override fun close(cause: Throwable?) {
        super.close(cause)
        (rx as Channel<FullMessage>).close(cause)
    }

    internal suspend fun handleSession(session: WebSocketSession) = session.run {
        try {
            var continues = true
            while (continues) {
                select {
                    (tx as Channel<FullMessage>).onReceiveCatching {
                        if (it.isClosed) {
                            continues = false
                        } else {
                            send(frameTransport.encodeMessage(it.getOrThrow()))
                        }
                    }

                    incoming.onReceiveCatching {
                        if (it.isClosed) {
                            continues = false
                        } else {
                            val frame = it.getOrThrow()
                            if(frame is Frame.Close) {
                                continues = false
                                return@onReceiveCatching
                            }
                            try {
                                frameTransport.decodeMessage(frame)
                            } catch(e: SerializationException) {
                                logger.error(e) { "Serialization exception while decoding message" }
                                send(frameTransport.encodeMessage(wrap(Message.Error("Internal serialization error"))))
                                null
                            } catch(e: IllegalArgumentException) {
                                logger.warn(e) { "Invalid input" }
                                send(frameTransport.encodeMessage(wrap(Message.Error("Invalid message: ${e.message}"))))
                                null
                            }?.let { msg ->
                                (rx as Channel<FullMessage>).send(msg)
                            }
                        }
                    }
                }
            }
        } finally {
            this@WebSocketConnectionImpl.close()
        }
    }
}


