package net.derfruhling.html.ktor.server

import io.ktor.util.logging.Logger as KtorLogger

expect fun createKtorLogger(): KtorLogger
