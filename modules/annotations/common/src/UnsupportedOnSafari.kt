package net.derfruhling.serenity.annotations

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(message = "Unsupported on Safari.")
@MustBeDocumented
annotation class UnsupportedOnSafari
