package net.derfruhling.serenity.annotations

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(message = "Unsupported on Firefox browsers.")
@MustBeDocumented
annotation class UnsupportedOnFirefox
