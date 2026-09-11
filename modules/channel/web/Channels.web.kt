package net.derfruhling.serenity.channel

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.derfruhling.serenity.manifest.Manifest
import net.derfruhling.serenity.modularity.CallableEvent
import net.derfruhling.serenity.modularity.Event
import net.derfruhling.serenity.modularity.invoke
import web.scheduling.queueMicrotask
import web.window.window
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun interface CancellationContext {
    fun cancel()
}

actual object Channels {
    init {
        check(ChannelModule.isInitialized) {
            "You must activate ChannelModule before using anything in the Channels object."
        }
    }

    private lateinit var _connection: Connection
    private var _sessionId: Int = -1
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val sessionId: Int by lazy {
        if(_sessionId < 0) {
            throw IllegalStateException("Session not initialized yet")
        }

        _sessionId
    }

    val connection: Connection by ::_connection
    val onMessage: Event<suspend (CancellationContext, Int, Message) -> Unit> field = CallableEvent()

    private val logger = KotlinLogging.logger {}
    private var failedAttempts = 0

    private var _delayDuration: Duration = Duration.ZERO
    private val delayDuration: Duration
        get() = when(failedAttempts) {
            0 -> Duration.ZERO
            in 1..3 -> 5.seconds.also { _delayDuration = it }
            else -> {
                _delayDuration *= 2
                _delayDuration
            }
        }

    internal actual suspend fun init(manifest: Manifest) {
        failedAttempts = 0
        reconnect(manifest)
    }

    private tailrec suspend fun reconnect(manifest: Manifest) {
        if(failedAttempts > 10) {
            logger.error { "Too many failed attempts at reconnecting" }
            return
        }

        if (::_connection.isInitialized) {
            _connection.close()
            _sessionId = -1
        }

        try {
            _connection = ChannelModule.config.impl.connect(manifest)
            failedAttempts = 0
            scope.launch { manageConnection(manifest) }
            return
        } catch(e: Exception) {
            val attId = ++failedAttempts
            val delayDuration = delayDuration
            logger.warn(e) { "Attempt #$attId at connection failed, trying again in ${delayDuration.inWholeSeconds} seconds" }
            delay(delayDuration)
        }

        reconnect(manifest)
    }

    private suspend fun manageConnection(manifest: Manifest) {
        logger.info { "Connected to channel: $_connection" }
        _connection.receiveAsFlow()
            .collect { (msgId, message) -> eachMessage(msgId, message) }
        logger.warn { "Disconnected! Trying to reconnect..." }
        reconnect(manifest)
    }

    private suspend fun eachMessage(msgId: Int, message: Message) {
        if (message !is Message.S2C) {
            _connection.send(
                Message.Error(
                    "Message type ${message::class.simpleName ?: "<null>"} is not S2C",
                    messageId = msgId
                )
            )
        } else {
            var isCancelled = false
            val cCtx = CancellationContext { isCancelled = true }
            onMessage(cCtx, msgId, message)

            if(isCancelled && message.isCancellable) return

            when(message) {
                is Message.Hello -> {
                    _sessionId = message.sessionId
                }

                is Message.InvalidateKey -> {
                    for(key in message.keys) {
                        invalidate(key)
                    }
                }

                Message.Refresh -> {
                    queueMicrotask { window.location.reload() }
                }
            }
        }
    }

    suspend fun send(message: Message) {
        _connection.send(message)
    }

    fun sendIn(scope: CoroutineScope, message: Message): Job {
        return scope.launch { send(message) }
    }

    fun sendLater(message: Message): Job {
        return scope.launch { send(message) }
    }
}
