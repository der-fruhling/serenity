package net.derfruhling.serenity.annotations

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(message = "Unsupported on Chrome-like browsers.")
@MustBeDocumented
annotation class UnsupportedOnChromium
