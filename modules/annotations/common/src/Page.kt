package net.derfruhling.serenity.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
@Intrinsic
annotation class Page(
    val path: String
)
