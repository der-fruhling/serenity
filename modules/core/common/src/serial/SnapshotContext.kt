package net.derfruhling.serenity.serial

import androidx.compose.runtime.snapshots.Snapshot
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

class SnapshotContext(val snapshot: Snapshot) : ContinuationInterceptor {
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        return Continuation(continuation.context) {
            snapshot.enter {
                continuation.resumeWith(it)
            }
        }
    }

    override val key: CoroutineContext.Key<*>
        get() = ContinuationInterceptor.Key
}
