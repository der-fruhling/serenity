package net.derfruhling.html

import androidx.compose.runtime.snapshots.Snapshot
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

class SnapshotContext(val snapshot: Snapshot) : AbstractCoroutineContextElement(SnapshotContext), ContinuationInterceptor {
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        return Continuation(continuation.context) {
            snapshot.enter {
                continuation.resumeWith(it)
            }
        }
    }

    companion object : CoroutineContext.Key<SnapshotContext>
}
