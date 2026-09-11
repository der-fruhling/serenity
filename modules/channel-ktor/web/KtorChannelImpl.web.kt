package net.derfruhling.serenity.channel.ktor

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import net.derfruhling.serenity.channel.*
import net.derfruhling.serenity.manifest.Manifest
import web.url.URL
import web.window.window

actual class KtorChannelConfig {
    actual var endpoint: String = DEFAULT_ENDPOINT
    actual var maxFrameSize: Long? = null
    actual var pingIntervalMillis: Long? = null
    actual var frameTransport: FrameTransport = JsonFrameTransport

    private var clientSupplier: () -> HttpClient = {
        HttpClient(Js) {
            install(WebSockets) {
                channels {
                    incoming = unlimited()
                    outgoing = bounded(8)
                }

                this@KtorChannelConfig.maxFrameSize?.let { maxFrameSize = it }
                this@KtorChannelConfig.pingIntervalMillis?.let { pingIntervalMillis = it }
            }
        }
    }

    fun withClient(fn: () -> HttpClient) {
        clientSupplier = fn
    }

    internal fun getClient(): HttpClient = clientSupplier()
}

actual class KtorChannelImpl : ChannelImpl<KtorChannelConfig> {
    private lateinit var client: HttpClient
    private lateinit var endpoint: String
    private lateinit var frameTransport: FrameTransport

    actual override fun configure(fn: KtorChannelConfig.() -> Unit) {
        KtorChannelConfig().apply(fn).let {
            client = it.getClient()
            endpoint = it.endpoint
            frameTransport = it.frameTransport
        }
    }

    override suspend fun connect(manifest: Manifest): Connection {
        val tx = Channel<FullMessage>()
        val rx = Channel<FullMessage>()
        val conn = WebSocketConnectionImpl(tx, rx, frameTransport)

        val url = if(endpoint.startsWith('/')) {
            URL(endpoint, window.location.href)
        } else URL(endpoint)

        url.protocol = when(val p = url.protocol) {
            "http:" -> "ws:"
            "https:" -> "wss:"
            else -> p
        }

        val session = client.webSocketSession(url.href)
        client.launch {
            conn.handleSession(session)
        }

        return conn
    }
}
