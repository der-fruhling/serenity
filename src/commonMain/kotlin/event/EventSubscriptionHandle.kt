package net.derfruhling.html.event

fun interface EventSubscriptionHandle {
    fun unsubscribe()

    object NoOp : EventSubscriptionHandle {
        override fun unsubscribe() {}
    }
}