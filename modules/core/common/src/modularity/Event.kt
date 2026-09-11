package net.derfruhling.serenity.modularity

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName

@JvmInline
value class EventSubscription<T : Function<Unit>> internal constructor(internal val value: T)

sealed class Event<T : Function<Unit>>(private val listeners: MutableSet<T> = mutableSetOf()) {
    fun subscribe(fn: T): EventSubscription<T> {
        listeners.add(fn)
        return EventSubscription(fn)
    }

    @JvmName("unsubscribeByValue")
    fun unsubscribe(fn: T) {
        listeners.remove(fn)
    }

    fun unsubscribe(fn: EventSubscription<T>) {
        listeners.remove(fn.value)
    }

    fun snapshotListeners(): Set<T> = listeners.toSet()

    inline fun fire(fn: (T) -> Unit) {
        for(f in snapshotListeners()) {
            fn(f)
        }
    }
}

class CallableEvent<T : Function<Unit>> : Event<T>()

fun <T : Function<Unit>> Event() = CallableEvent<T>()

operator fun CallableEvent<() -> Unit>.invoke() = fire { it() }

@JvmName("invoke1")
operator fun <T1> CallableEvent<(T1) -> Unit>.invoke(t1: T1) = fire { it(t1) }

@JvmName("invoke2")
operator fun <T1, T2> CallableEvent<(T1, T2) -> Unit>.invoke(t1: T1, t2: T2) = fire { it(t1, t2) }

@JvmName("invoke3")
operator fun <T1, T2, T3> CallableEvent<(T1, T2, T3) -> Unit>.invoke(t1: T1, t2: T2, t3: T3) = fire { it(t1, t2, t3) }

suspend operator fun CallableEvent<suspend () -> Unit>.invoke() = fire { it() }

@JvmName("invoke1")
suspend operator fun <T1> CallableEvent<suspend (T1) -> Unit>.invoke(t1: T1) = fire { it(t1) }

@JvmName("invoke2")
suspend operator fun <T1, T2> CallableEvent<suspend (T1, T2) -> Unit>.invoke(t1: T1, t2: T2) = fire { it(t1, t2) }

@JvmName("invoke2")
suspend operator fun <T1, T2, T3> CallableEvent<suspend (T1, T2, T3) -> Unit>.invoke(t1: T1, t2: T2, t3: T3) = fire { it(t1, t2, t3) }
