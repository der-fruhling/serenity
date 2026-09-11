package net.derfruhling.serenity.channel

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.derfruhling.serenity.manifest.Manifest
import net.derfruhling.serenity.modularity.CallableEvent
import net.derfruhling.serenity.modularity.Event
import net.derfruhling.serenity.modularity.EventSubscription
import net.derfruhling.serenity.modularity.invoke

actual object Channels {
    init {
        check(ChannelModule.isInitialized) {
            "You must activate ChannelModule before using anything in the Channels object."
        }
    }

    private lateinit var channel: ChannelImpl<*>
    private var connectSubscriptionHandle: EventSubscription<suspend (Int, Connection) -> Unit>? =
        null
    private var disconnectSubscriptionHandle: EventSubscription<suspend (Int, Connection) -> Unit>? =
        null
    private val connectionMutex = Mutex()
    private val connections = mutableMapOf<Int, Connection>()
    private val logger = KotlinLogging.logger {}
    private lateinit var scope: CoroutineScope

    val onMessage: Event<suspend (Int, Connection, Message) -> Unit> field = CallableEvent()

    internal actual suspend fun init(manifest: Manifest) {
        if (::channel.isInitialized && channel != ChannelModule.config.impl) {
            connectSubscriptionHandle?.let { channel.onConnect.unsubscribe(it) }
            disconnectSubscriptionHandle?.let { channel.onDisconnect.unsubscribe(it) }
            if(channel.coroutineScope != null) scope = CoroutineScope(SupervisorJob())
            channel.close()
        }

        channel = ChannelModule.config.impl
        connectSubscriptionHandle = channel.onConnect.subscribe(this::onConnect)
        disconnectSubscriptionHandle = channel.onDisconnect.subscribe(this::onDisconnect)
        scope = channel.coroutineScope ?: if(::scope.isInitialized) {
            scope
        } else {
            CoroutineScope(SupervisorJob())
        }
    }

    private suspend fun onConnect(id: Int, connection: Connection) = connectionMutex.withLock {
        val existing = connections.put(id, connection)

        if (existing != null) {
            logger.warn { "Connection $connection with id $id replaced existing connection $existing; this is probably a bug, the old connection has been forgotten" }
        }

        scope.launch {
            connection.receiveAsFlow()
                .onEach { (msgId, message) ->
                    if (message !is Message.C2S) {
                        connection.send(
                            Message.Error(
                                "Message type ${message::class.qualifiedName!!} is not C2S",
                                messageId = msgId
                            )
                        )
                    } else {
                        onMessage(msgId, connection, message)
                    }
                }
                .launchIn(this)
        }
    }

    private suspend fun onDisconnect(id: Int, connection: Connection) {
        connections.remove(id)
    }

    suspend fun sendAll(message: Message) = connectionMutex.withLock {
        coroutineScope {
            for (conn in connections.values) {
                launch { conn.send(message) }
            }
        }
    }
}
