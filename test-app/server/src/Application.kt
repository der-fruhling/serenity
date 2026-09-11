package net.derfruhling.serenity.testapp

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.derfruhling.serenity.channel.Channels
import net.derfruhling.serenity.channel.Message
import net.derfruhling.serenity.channel.ktor.KtorChannel
import net.derfruhling.serenity.server.ktor.Serenity
import net.derfruhling.serenity.server.ktor.registerServerPages
import net.derfruhling.serenity.server.ktor.serveStatic
import kotlin.time.Duration.Companion.milliseconds

fun Application.configure() {
    install(Serenity)
    install(KtorChannel)

    launch {
        while(true) {
            Channels.sendAll(Message.InvalidateKey("timer"))
            delay(1500.milliseconds)
        }
    }

    routing {
        serveStatic()

        registerServerPages { registerPages() }
    }
}
