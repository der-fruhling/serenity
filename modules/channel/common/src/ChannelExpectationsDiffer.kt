package net.derfruhling.serenity.channel

@RequiresOptIn("ChannelImpl is implemented differently in server and web sources", level = RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class ChannelExpectationsDiffer
