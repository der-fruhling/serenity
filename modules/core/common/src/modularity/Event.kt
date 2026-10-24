package net.derfruhling.serenity.modularity

import kotlin.jvm.JvmName

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
        for (f in snapshotListeners()) {
            fn(f)
        }
    }
}

