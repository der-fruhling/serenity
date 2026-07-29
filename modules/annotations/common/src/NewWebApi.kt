package net.derfruhling.serenity.annotations

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING, message = "This API is new and may not be supported on all browsers.")
annotation class NewWebApi
