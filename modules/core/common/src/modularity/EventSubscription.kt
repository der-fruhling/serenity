package net.derfruhling.serenity.modularity

import kotlin.jvm.JvmInline

@JvmInline
value class EventSubscription<T : Function<Unit>> internal constructor(internal val value: T)
