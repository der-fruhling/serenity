package net.derfruhling.serenity.server.ktor

import io.ktor.util.logging.Logger as KtorLogger

expect fun createKtorLogger(): KtorLogger
