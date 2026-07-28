package net.derfruhling.serenity.annotations

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(message = "Unsupported on Safari.")
@MustBeDocumented
annotation class UnsupportedOnSafari

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(message = "Unsupported on Chrome-like browsers.")
@MustBeDocumented
annotation class UnsupportedOnChromium

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(message = "Unsupported on Firefox browsers.")
@MustBeDocumented
annotation class UnsupportedOnFirefox

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(message = "Unsupported on Opera browsers.")
@MustBeDocumented
annotation class UnsupportedOnOpera
