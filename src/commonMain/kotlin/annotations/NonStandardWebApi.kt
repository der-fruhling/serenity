package net.derfruhling.serenity.annotations

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "This API is non-standard. Do not rely on this.")
@MustBeDocumented
annotation class NonStandardWebApi
