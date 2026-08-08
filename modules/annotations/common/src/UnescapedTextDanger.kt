package net.derfruhling.serenity.annotations

@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "This method does NOT escape the provided text. You must not use user input here, as doing so may be unsafe.")
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
annotation class UnescapedTextDanger()
