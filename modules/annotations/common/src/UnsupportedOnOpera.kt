package net.derfruhling.serenity.annotations

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(message = "Unsupported on Opera browsers.")
@MustBeDocumented
annotation class UnsupportedOnOpera
