package net.derfruhling.serenity.ktor.server

import io.ktor.util.logging.Logger as KtorLogger

expect fun createKtorLogger(): KtorLogger
