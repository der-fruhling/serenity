package net.derfruhling.serenity.channel.ktor

import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import net.derfruhling.serenity.channel.ChannelExpectationsDiffer
import net.derfruhling.serenity.channel.ChannelImpl
import net.derfruhling.serenity.channel.ChannelModule
import net.derfruhling.serenity.channel.Connection
import net.derfruhling.serenity.channel.FullMessage
import net.derfruhling.serenity.channel.Message
import net.derfruhling.serenity.channel.ServerChannelImpl
import net.derfruhling.serenity.modularity.CallableEvent
import net.derfruhling.serenity.modularity.Event
import net.derfruhling.serenity.modularity.Modules
import net.derfruhling.serenity.modularity.invoke

actual class KtorChannelConfig {
    actual var endpoint: String = DEFAULT_ENDPOINT
    actual var maxFrameSize: Long? = null
    actual var pingIntervalMillis: Long? = null
    var timeoutMillis: Long? = null
    actual var frameTransport: FrameTransport = JsonFrameTransport
}

actual class KtorChannelImpl : ChannelImpl<KtorChannelConfig>, ServerChannelImpl<KtorChannelConfig> {
    internal lateinit var config: KtorChannelConfig
    private lateinit var _endpoint: String
    private lateinit var frameTransport: FrameTransport
    private val sessionIdCounter = atomic(0)

    actual override fun configure(fn: KtorChannelConfig.() -> Unit) {
        KtorChannelConfig().apply(fn).also {
            config = it
            _endpoint = it.endpoint
            frameTransport = it.frameTransport
        }
    }

    private val _onConnect: CallableEvent<suspend (Int, Connection) -> Unit> = CallableEvent()
    private val _onDisconnect: CallableEvent<suspend (Int, Connection) -> Unit> = CallableEvent()

    override val onConnect: Event<suspend (Int, Connection) -> Unit> by ::_onConnect
    override val onDisconnect: Event<suspend (Int, Connection) -> Unit> by ::_onDisconnect

    suspend fun runSocket(session: WebSocketServerSession) {
        val tx = Channel<FullMessage>()
        val rx = Channel<FullMessage>()
        val conn = WebSocketConnectionImpl(sessionIdCounter.getAndIncrement(), tx, rx, frameTransport)
        session.launch { conn.send(Message.Hello(conn.id)) }
        _onConnect(conn.id, conn)
        try {
            conn.handleSession(session)
        } finally {
            _onDisconnect(conn.id, conn)
        }
    }

    override fun close() {}
}

val KtorChannel = createApplicationPlugin("(serenity) KtorChannel") {
    val mod = Modules[ChannelModule::class]
        ?: throw IllegalStateException("Before installing the KtorChannel plugin, you must install ChannelModule as a Serenity module")

    val impl = mod.config.impl
    check(impl is KtorChannelImpl) {
        "The provided ChannelImpl must extend KtorChannelImpl"
    }

    application.install(WebSockets) {
        impl.config.pingIntervalMillis?.let { pingPeriodMillis = it }
        impl.config.timeoutMillis?.let { timeoutMillis = it }
        impl.config.maxFrameSize?.let { maxFrameSize = it }

        channels {
            incoming = bounded(8)
            outgoing = bounded(32)
        }
    }

    application.routing {
        webSocket(impl.config.endpoint) {
            impl.runSocket(this)
        }
    }
}
