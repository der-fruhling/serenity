package net.derfruhling.serenity.event

fun interface EventSubscriptionHandle {
    fun unsubscribe()

    object NoOp : EventSubscriptionHandle {
        override fun unsubscribe() {}
    }
}