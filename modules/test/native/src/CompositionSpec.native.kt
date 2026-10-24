package net.derfruhling.serenity.test

import androidx.compose.runtime.BroadcastFrameClock
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.TimeSource

internal actual suspend inline fun <T> withFrameClock(crossinline fn: suspend CoroutineScope.() -> T): T {
    val clock = BroadcastFrameClock()
    return withContext(clock) {
        val job = launch {
            val from = TimeSource.Monotonic.markNow()

            while (true) {
                clock.sendFrame(from.elapsedNow().inWholeNanoseconds)
                delay(16666666.nanoseconds)
            }
        }

        try {
            fn()
        } finally {
            clock.cancel()
            job.cancelAndJoin()
        }
    }
}
