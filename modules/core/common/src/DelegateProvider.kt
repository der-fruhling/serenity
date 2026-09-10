package net.derfruhling.serenity

import kotlin.reflect.KProperty

fun interface DelegateProvider<S, T> {
    operator fun provideDelegate(self: S, property: KProperty<*>): T
}
