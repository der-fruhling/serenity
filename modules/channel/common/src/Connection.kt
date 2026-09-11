package net.derfruhling.serenity.channel

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel

interface Connection : ReceiveChannel<FullMessage> {
    val id: Int

    fun close(cause: Throwable? = null)
    suspend fun send(message: Message): Int
    fun wrap(message: Message): FullMessage
    fun trySend(message: FullMessage): ChannelResult<Unit>
}

abstract class AbstractConnection(
    override val id: Int,
    protected val tx: SendChannel<FullMessage>,
    protected val rx: ReceiveChannel<FullMessage>
) : Connection, ReceiveChannel<FullMessage> by rx {
    private val idCounter = atomic(0)

    override fun wrap(message: Message): FullMessage = FullMessage(idCounter.getAndIncrement(), message)

    override fun trySend(message: FullMessage): ChannelResult<Unit> {
        return tx.trySend(message)
    }

    override suspend fun send(message: Message): Int {
        val wrap = wrap(message)
        tx.send(wrap)
        return wrap.id
    }

    override fun close(cause: Throwable?) {
        tx.close(cause)
    }
}
