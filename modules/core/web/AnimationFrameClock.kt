package net.derfruhling.serenity

import androidx.compose.runtime.MonotonicFrameClock
import js.numbers.JsNumbers.toKotlinDouble
import kotlinx.coroutines.suspendCancellableCoroutine
import web.time.DOMHighResTimeStamp
import kotlin.coroutines.resume
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(fn) => window.requestAnimationFrame(fn)")
private external fun windowRequestAnimationFrame(fn: (DOMHighResTimeStamp) -> Unit): Int

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(id) => window.cancelAnimationFrame(id)")
private external fun windowCancelAnimationFrame(id: Int)

@Suppress("UnnecessaryOptInAnnotation")
@OptIn(ExperimentalWasmJsInterop::class)
object AnimationFrameClock : MonotonicFrameClock {
    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
        return suspendCancellableCoroutine { continuation ->
            val id = windowRequestAnimationFrame {
                val duration = it.toKotlinDouble().toDuration(DurationUnit.MILLISECONDS)
                val result = onFrame(duration.inWholeNanoseconds)
                continuation.resume(result)
            }

            continuation.invokeOnCancellation {
                windowCancelAnimationFrame(id)
            }
        }
    }
}