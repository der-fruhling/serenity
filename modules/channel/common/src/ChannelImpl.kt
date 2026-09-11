package net.derfruhling.serenity.channel

@SubclassOptInRequired(ChannelExpectationsDiffer::class)
expect interface ChannelImpl<C> {
    fun configure(fn: C.() -> Unit)
}
