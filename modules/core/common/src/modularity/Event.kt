package net.derfruhling.serenity.modularity

import kotlin.jvm.JvmName

sealed class Event<T : Function<Unit>>(private val listeners: MutableSet<T> = mutableSetOf()) {
    fun subscribe(fn: T) {
        listeners.add(fn)
    }

    fun unsubscribe(fn: T) {
        listeners.remove(fn)
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
