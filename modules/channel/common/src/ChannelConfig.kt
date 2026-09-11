package net.derfruhling.serenity.channel

class ChannelConfig internal constructor() {
    private lateinit var _impl: ChannelImpl<*>

    val impl: ChannelImpl<*> by ::_impl

    fun <C> withImpl(impl: ChannelImpl<C>, fn: C.() -> Unit = {}) {
        _impl = impl
        impl.configure(fn)
    }
}
